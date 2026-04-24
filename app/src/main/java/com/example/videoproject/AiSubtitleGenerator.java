package com.example.videoproject;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Generates subtitle files from local video files using the AssemblyAI speech-to-text API.
 *
 * Mental model:
 * 1) Upload the local video file to AssemblyAI.
 * 2) Ask AssemblyAI to transcribe it with timestamps.
 * 3) Convert timestamped words into SRT cues.
 * 4) Save the generated SRT into the app's private files directory.
 */
public final class AiSubtitleGenerator {

    private static final String PREF_NAME = "ai_subtitle_settings";
    private static final String KEY_API_KEY = "assemblyai_api_key";
    private static final String DEFAULT_API_KEY = "908f4f7b88cb4617bf84dcc80117a9f6";

    private static final String UPLOAD_URL = "https://api.assemblyai.com/v2/upload";
    private static final String TRANSCRIPT_URL = "https://api.assemblyai.com/v2/transcript";

    private static final int BUFFER_SIZE = 16 * 1024;
    private static final int MAX_POLL_ATTEMPTS = 60;
    private static final int POLL_DELAY_MS = 4000;

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private AiSubtitleGenerator() {
    }

    public interface Callback {
        void onStarted();

        void onSuccess(File subtitleFile);

        void onError(String message);
    }

    public static void saveApiKey(Context context, String apiKey) {
        if (context == null) {
            return;
        }
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_API_KEY, apiKey == null ? "" : apiKey.trim())
                .apply();
    }

    public static String getApiKey(Context context) {
        if (context == null) {
            return DEFAULT_API_KEY;
        }
        String apiKey = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_API_KEY, "");
        String trimmedApiKey = apiKey == null ? "" : apiKey.trim();
        return TextUtils.isEmpty(trimmedApiKey) ? DEFAULT_API_KEY : trimmedApiKey;
    }

    public static boolean hasApiKey(Context context) {
        return !TextUtils.isEmpty(getApiKey(context));
    }

    public static File getGeneratedSubtitleFile(Context context, String videoPath) {
        File subtitleDir = new File(context.getFilesDir(), "generated_subtitles");
        if (!subtitleDir.exists()) {
            subtitleDir.mkdirs();
        }
        return new File(subtitleDir, buildSubtitleBaseName(videoPath) + ".srt");
    }

    public static void generateSubtitles(final Context context,
                                         final String videoPath,
                                         final Callback callback) {
        if (context == null) {
            postError(callback, "Context is null");
            return;
        }

        final String apiKey = getApiKey(context);
        if (TextUtils.isEmpty(apiKey)) {
            postError(callback, "API key is missing");
            return;
        }

        if (TextUtils.isEmpty(videoPath)) {
            postError(callback, "Video path is missing");
            return;
        }

        final File videoFile = new File(videoPath);
        if (!videoFile.exists() || !videoFile.isFile()) {
            postError(callback, "Video file does not exist");
            return;
        }

        postStarted(callback);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String uploadedMediaUrl = uploadVideoFile(apiKey, videoFile);
                    String transcriptId = requestTranscript(apiKey, uploadedMediaUrl);
                    List<TranscriptWord> words = pollTranscriptWords(apiKey, transcriptId);

                    if (words.isEmpty()) {
                        throw new IOException("Transcript completed but returned no timestamped words");
                    }

                    List<SrtCue> cues = buildSrtCues(words);
                    if (cues.isEmpty()) {
                        throw new IOException("Unable to build subtitle cues from transcript");
                    }

                    File subtitleFile = getGeneratedSubtitleFile(context, videoPath);
                    writeSrtFile(subtitleFile, cues);
                    postSuccess(callback, subtitleFile);
                } catch (Exception e) {
                    postError(callback, toReadableError(e));
                }
            }
        }, "ai-subtitle-generator").start();
    }

    private static String uploadVideoFile(String apiKey, File videoFile) throws IOException, JSONException {
        HttpURLConnection connection = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;

        try {
            connection = (HttpURLConnection) new URL(UPLOAD_URL).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", apiKey);
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            connection.setFixedLengthStreamingMode(videoFile.length());
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(300000);

            outputStream = new BufferedOutputStream(connection.getOutputStream());
            inputStream = new BufferedInputStream(new FileInputStream(videoFile));

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();

            int responseCode = connection.getResponseCode();
            String responseBody = readResponseBody(connection);
            if (responseCode < 200 || responseCode >= 300) {
                throw new IOException(extractApiError(responseBody, "Upload failed"));
            }

            JSONObject responseJson = new JSONObject(responseBody);
            String uploadUrl = responseJson.optString("upload_url", "");
            if (TextUtils.isEmpty(uploadUrl)) {
                throw new IOException("Upload succeeded but no upload_url was returned");
            }
            return uploadUrl;
        } finally {
            closeQuietly(inputStream);
            closeQuietly(outputStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String requestTranscript(String apiKey, String uploadedMediaUrl)
            throws IOException, JSONException {
        HttpURLConnection connection = null;
        OutputStream outputStream = null;

        try {
            connection = (HttpURLConnection) new URL(TRANSCRIPT_URL).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", apiKey);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);

            JSONObject requestJson = new JSONObject();
            requestJson.put("audio_url", uploadedMediaUrl);
            requestJson.put("language_detection", true);

            JSONArray speechModels = new JSONArray();
            speechModels.put("universal-2");
            requestJson.put("speech_models", speechModels);

            byte[] bodyBytes = requestJson.toString().getBytes("UTF-8");
            connection.setFixedLengthStreamingMode(bodyBytes.length);
            outputStream = connection.getOutputStream();
            outputStream.write(bodyBytes);
            outputStream.flush();

            int responseCode = connection.getResponseCode();
            String responseBody = readResponseBody(connection);
            if (responseCode < 200 || responseCode >= 300) {
                throw new IOException(extractApiError(responseBody, "Transcript request failed"));
            }

            JSONObject responseJson = new JSONObject(responseBody);
            String transcriptId = responseJson.optString("id", "");
            if (TextUtils.isEmpty(transcriptId)) {
                throw new IOException("Transcript request succeeded but no transcript id was returned");
            }
            return transcriptId;
        } finally {
            closeQuietly(outputStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static List<TranscriptWord> pollTranscriptWords(String apiKey, String transcriptId)
            throws IOException, JSONException {
        for (int attempt = 0; attempt < MAX_POLL_ATTEMPTS; attempt++) {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(TRANSCRIPT_URL + "/" + transcriptId).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", apiKey);
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(60000);

                int responseCode = connection.getResponseCode();
                String responseBody = readResponseBody(connection);
                if (responseCode < 200 || responseCode >= 300) {
                    throw new IOException(extractApiError(responseBody, "Transcript polling failed"));
                }

                JSONObject responseJson = new JSONObject(responseBody);
                String status = responseJson.optString("status", "");
                if ("completed".equalsIgnoreCase(status)) {
                    return parseWords(responseJson.optJSONArray("words"));
                }

                if ("error".equalsIgnoreCase(status)) {
                    throw new IOException(responseJson.optString("error", "Transcription failed"));
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            try {
                Thread.sleep(POLL_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Transcript polling interrupted", e);
            }
        }

        throw new IOException("Timed out while waiting for speech-to-text result");
    }

    private static List<TranscriptWord> parseWords(JSONArray wordsArray) {
        List<TranscriptWord> words = new ArrayList<>();
        if (wordsArray == null) {
            return words;
        }

        for (int i = 0; i < wordsArray.length(); i++) {
            JSONObject wordJson = wordsArray.optJSONObject(i);
            if (wordJson == null) {
                continue;
            }

            String text = wordJson.optString("text", "").trim();
            long start = wordJson.optLong("start", -1);
            long end = wordJson.optLong("end", -1);
            if (TextUtils.isEmpty(text) || start < 0 || end <= start) {
                continue;
            }

            words.add(new TranscriptWord(start, end, text));
        }

        return words;
    }

    private static List<SrtCue> buildSrtCues(List<TranscriptWord> words) {
        List<SrtCue> cues = new ArrayList<>();
        if (words == null || words.isEmpty()) {
            return cues;
        }

        StringBuilder textBuilder = new StringBuilder();
        long cueStart = words.get(0).startMs;
        long cueEnd = words.get(0).endMs;
        int wordCount = 0;
        TranscriptWord previousWord = null;

        for (int i = 0; i < words.size(); i++) {
            TranscriptWord word = words.get(i);
            if (wordCount == 0) {
                cueStart = word.startMs;
                textBuilder.setLength(0);
            }

            if (textBuilder.length() > 0) {
                textBuilder.append(' ');
            }
            textBuilder.append(word.text);
            cueEnd = word.endMs;
            wordCount++;

            TranscriptWord nextWord = i + 1 < words.size() ? words.get(i + 1) : null;
            boolean gapBreak = nextWord != null && nextWord.startMs - word.endMs > 1200;
            boolean durationBreak = cueEnd - cueStart >= 4000;
            boolean wordBreak = wordCount >= 8;
            boolean textBreak = textBuilder.length() >= 48;
            boolean finalWord = nextWord == null;

            if (gapBreak || durationBreak || wordBreak || textBreak || finalWord) {
                cues.add(new SrtCue(cueStart, cueEnd, textBuilder.toString().trim()));
                textBuilder.setLength(0);
                wordCount = 0;
            }

            previousWord = word;
        }

        if (textBuilder.length() > 0 && previousWord != null) {
            cues.add(new SrtCue(cueStart, previousWord.endMs, textBuilder.toString().trim()));
        }

        return cues;
    }

    private static void writeSrtFile(File subtitleFile, List<SrtCue> cues) throws IOException {
        File parent = subtitleFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(subtitleFile, false), "UTF-8"));

            for (int i = 0; i < cues.size(); i++) {
                SrtCue cue = cues.get(i);
                writer.write(String.valueOf(i + 1));
                writer.newLine();
                writer.write(formatSrtTimestamp(cue.startMs)
                        + " --> "
                        + formatSrtTimestamp(cue.endMs));
                writer.newLine();
                writer.write(cue.text);
                writer.newLine();
                writer.newLine();
            }

            writer.flush();
        } finally {
            closeQuietly(writer);
        }
    }

    private static String formatSrtTimestamp(long timeMs) {
        long totalMilliseconds = Math.max(0, timeMs);
        long hours = totalMilliseconds / 3600000L;
        long minutes = (totalMilliseconds % 3600000L) / 60000L;
        long seconds = (totalMilliseconds % 60000L) / 1000L;
        long milliseconds = totalMilliseconds % 1000L;

        return String.format(Locale.US,
                "%02d:%02d:%02d,%03d",
                hours,
                minutes,
                seconds,
                milliseconds);
    }

    private static String buildSubtitleBaseName(String videoPath) {
        String baseName = "generated_subtitle";
        if (!TextUtils.isEmpty(videoPath)) {
            String fileName = new File(videoPath).getName();
            int dotIndex = fileName.lastIndexOf('.');
            baseName = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
        }

        baseName = baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (TextUtils.isEmpty(baseName)) {
            baseName = "generated_subtitle";
        }

        return baseName + "_" + Integer.toHexString(videoPath.hashCode());
    }

    private static String readResponseBody(HttpURLConnection connection) throws IOException {
        InputStream stream = null;
        BufferedReader reader = null;
        try {
            stream = connection.getResponseCode() >= 400
                    ? connection.getErrorStream()
                    : connection.getInputStream();

            if (stream == null) {
                return "";
            }

            reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } finally {
            closeQuietly(reader);
            closeQuietly(stream);
        }
    }

    private static String extractApiError(String responseBody, String fallbackMessage) {
        if (TextUtils.isEmpty(responseBody)) {
            return fallbackMessage;
        }

        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            String error = jsonObject.optString("error", "");
            if (!TextUtils.isEmpty(error)) {
                return error;
            }
        } catch (JSONException ignored) {
        }

        return responseBody;
    }

    private static String toReadableError(Exception exception) {
        if (exception == null || TextUtils.isEmpty(exception.getMessage())) {
            return "Subtitle generation failed";
        }
        return exception.getMessage();
    }

    private static void postStarted(final Callback callback) {
        if (callback == null) {
            return;
        }
        MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                callback.onStarted();
            }
        });
    }

    private static void postSuccess(final Callback callback, final File subtitleFile) {
        if (callback == null) {
            return;
        }
        MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess(subtitleFile);
            }
        });
    }

    private static void postError(final Callback callback, final String message) {
        if (callback == null) {
            return;
        }
        MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                callback.onError(message);
            }
        });
    }

    private static void closeQuietly(InputStream inputStream) {
        if (inputStream == null) {
            return;
        }
        try {
            inputStream.close();
        } catch (IOException ignored) {
        }
    }

    private static void closeQuietly(OutputStream outputStream) {
        if (outputStream == null) {
            return;
        }
        try {
            outputStream.close();
        } catch (IOException ignored) {
        }
    }

    private static void closeQuietly(BufferedReader reader) {
        if (reader == null) {
            return;
        }
        try {
            reader.close();
        } catch (IOException ignored) {
        }
    }

    private static void closeQuietly(BufferedWriter writer) {
        if (writer == null) {
            return;
        }
        try {
            writer.close();
        } catch (IOException ignored) {
        }
    }

    private static final class TranscriptWord {
        final long startMs;
        final long endMs;
        final String text;

        TranscriptWord(long startMs, long endMs, String text) {
            this.startMs = startMs;
            this.endMs = endMs;
            this.text = text;
        }
    }

    private static final class SrtCue {
        final long startMs;
        final long endMs;
        final String text;

        SrtCue(long startMs, long endMs, String text) {
            this.startMs = startMs;
            this.endMs = endMs;
            this.text = text;
        }
    }
}
