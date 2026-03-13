package com.example.musicproject;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for video-related operations.
 * Handles MediaStore queries, formatting, and thumbnail loading.
 */
public class VideoUtils {

    /**
     * Format duration from milliseconds to HH:MM:SS or MM:SS.
     *
     * @param durationMs Duration in milliseconds
     * @return Formatted time string
     */
    public static String formatDuration(long durationMs) {
        long totalSeconds = durationMs / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds);
        }
    }

    /**
     * Format file size to human-readable string.
     *
     * @param sizeBytes File size in bytes
     * @return Formatted size string (e.g., "1.5 GB", "350 MB")
     */
    public static String formatFileSize(long sizeBytes) {
        if (sizeBytes <= 0) return "0 B";

        double kb = sizeBytes / 1024.0;
        double mb = kb / 1024.0;
        double gb = mb / 1024.0;

        if (gb >= 1.0) {
            return String.format(Locale.US, "%.1f GB", gb);
        } else if (mb >= 1.0) {
            return String.format(Locale.US, "%.1f MB", mb);
        } else if (kb >= 1.0) {
            return String.format(Locale.US, "%.1f KB", kb);
        } else {
            return sizeBytes + " B";
        }
    }

    /**
     * Query all video files from MediaStore.
     *
     * @param context Application context
     * @return List of VideoItem objects
     */
    public static List<VideoItem> getAllVideos(Context context) {
        List<VideoItem> videoList = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();

        // Define projection columns
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT
        };

        // Query URI
        Uri queryUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        // Sort by date added descending (newest first)
        String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";

        Cursor cursor = null;
        try {
            cursor = resolver.query(queryUri, projection, null, null, sortOrder);

            if (cursor != null && cursor.moveToFirst()) {
                // Get column indices
                int idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                int nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                int pathCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                int durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                int sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                int dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);
                int dateModifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED);
                int mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);
                int widthCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH);
                int heightCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT);

                do {
                    long id = cursor.getLong(idCol);
                    String name = cursor.getString(nameCol);
                    String path = cursor.getString(pathCol);
                    long duration = cursor.getLong(durationCol);
                    long size = cursor.getLong(sizeCol);
                    long dateAdded = cursor.getLong(dateAddedCol);
                    long dateModified = cursor.getLong(dateModifiedCol);
                    String mimeType = cursor.getString(mimeCol);
                    int width = cursor.getInt(widthCol);
                    int height = cursor.getInt(heightCol);

                    // Skip files with zero duration (likely not valid videos)
                    if (duration > 0) {
                        VideoItem item = new VideoItem(id, name, path, duration,
                                size, dateAdded, dateModified, mimeType, width, height);
                        videoList.add(item);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return videoList;
    }

    /**
     * Load video thumbnail from MediaStore.
     *
     * @param context Application context
     * @param videoId MediaStore video ID
     * @return Bitmap thumbnail or null if not available
     */
    public static Bitmap loadThumbnail(Context context, long videoId) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ uses ContentResolver.loadThumbnail
                Uri uri = Uri.withAppendedPath(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        String.valueOf(videoId));
                return context.getContentResolver().loadThumbnail(uri, new Size(240, 135), null);
            } else {
                // Legacy thumbnail loading
                return MediaStore.Video.Thumbnails.getThumbnail(
                        context.getContentResolver(),
                        videoId,
                        MediaStore.Video.Thumbnails.MINI_KIND,
                        null);
            }
        } catch (Exception e) {
            // Thumbnail may not be available
            return null;
        }
    }

    /**
     * Find a VideoItem by its MediaStore ID from a list.
     *
     * @param videos List of videos
     * @param id     MediaStore ID to find
     * @return VideoItem or null
     */
    public static VideoItem findVideoById(List<VideoItem> videos, long id) {
        for (VideoItem v : videos) {
            if (v.getId() == id) {
                return v;
            }
        }
        return null;
    }

    /**
     * Find video index by path in a list.
     *
     * @param videos List of videos
     * @param path   File path
     * @return Index or -1
     */
    public static int findVideoIndexByPath(List<VideoItem> videos, String path) {
        if (path == null) return -1;
        for (int i = 0; i < videos.size(); i++) {
            if (path.equals(videos.get(i).getPath())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Format date from timestamp to readable string.
     *
     * @param timestampSeconds Timestamp in seconds
     * @return Formatted date string
     */
    public static String formatDate(long timestampSeconds) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new java.util.Date(timestampSeconds * 1000));
    }

    /**
     * Get video thumbnail (alias for loadThumbnail for backward compatibility).
     *
     * @param context Application context
     * @param videoId MediaStore video ID
     * @return Bitmap thumbnail or null if not available
     */
    public static Bitmap getVideoThumbnail(Context context, long videoId) {
        return loadThumbnail(context, videoId);
    }
}
