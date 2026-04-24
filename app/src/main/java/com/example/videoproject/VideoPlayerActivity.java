package com.example.videoproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnVideoSizeChangedListener {

    private static final int HIDE_CONTROLS_DELAY = 3000;
    private static final int UPDATE_PROGRESS_DELAY = 500;
    private static final int SEEK_SKIP_TIME = 10000;
    private static final int RESUME_PROMPT_THRESHOLD_MS = 5000;
    private static final int REPEAT_MODE_OFF = 0;
    private static final int REPEAT_MODE_ONE = 1;
    private static final int REPEAT_MODE_ALL = 2;

    private MediaPlayer mediaPlayer;
    private View playerRoot;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    private RelativeLayout controlsOverlay;
    private LinearLayout topBar;
    private LinearLayout bottomBar;
    private LinearLayout centerControls;
    private View lockOverlay;

    private ImageButton btnPlayPause;
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private ImageButton btnBack;
    private ImageButton btnFullscreen;
    private ImageButton btnLock;
    private ImageButton btnUnlock;
    private ImageButton btnSleepTimer;
    private ImageButton btnVideoInfo;
    private ImageButton btnRewind;
    private ImageButton btnForward;
    private ImageButton btnBookmark;
    private ImageButton btnNote;

    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    private TextView tvVideoTitle;
    private TextView tvSleepTimer;
    private TextView tvSubtitle;
    private TextView tvQualityBadge;
    private TextView btnSpeed;
    private TextView btnRepeat;
    private TextView btnSubtitle;
    private SeekBar seekBar;

    private LinearLayout brightnessOverlay;
    private ProgressBar brightnessProgress;
    private TextView tvBrightnessValue;

    private LinearLayout volumeOverlay;
    private ProgressBar volumeProgress;
    private TextView tvVolumeValue;

    private LinearLayout seekOverlay;
    private TextView tvSeekPosition;
    private TextView tvSeekOffset;

    private View doubleTapLeft;
    private View doubleTapRight;

    private ArrayList<String> videoPaths;
    private ArrayList<String> videoTitles;
    private long[] videoIds;
    private int currentIndex = 0;

    private boolean isControlsVisible = true;
    private boolean isLocked = false;
    private boolean isFullscreen = false;
    private boolean isPrepared = false;
    private boolean isSurfaceCreated = false;
    private boolean hasHandledResumePrompt = false;
    private float currentPlaybackSpeed = 1.0f;

    private GestureDetector gestureDetector;
    private AudioManager audioManager;
    private int maxVolume;
    private float currentBrightness = -1.0f;
    private int startVolume = -1;
    private long seekStartPos = -1;

    private final Handler handler = new Handler();
    private Runnable updateProgressRunnable;
    private Runnable hideControlsRunnable;
    private Runnable hideGesturesRunnable;
    private Runnable sleepTimerRunnable;

    private int sleepTimerMinutes = 0;
    private int repeatMode = REPEAT_MODE_OFF;
    private boolean subtitlesEnabled = false;
    private boolean subtitleFileAvailable = false;
    private final List<SubtitleCue> subtitleCues = new ArrayList<>();
    private int currentSubtitleIndex = -1;
    private boolean isGeneratingAiSubtitles = false;
    private String generatingSubtitleVideoPath = null;

    private static class SubtitleCue {
        final long startMs;
        final long endMs;
        final String text;

        SubtitleCue(long startMs, long endMs, String text) {
            this.startMs = startMs;
            this.endMs = endMs;
            this.text = text;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_player);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        videoPaths = getIntent().getStringArrayListExtra("video_paths");
        videoTitles = getIntent().getStringArrayListExtra("video_titles");
        videoIds = getIntent().getLongArrayExtra("video_ids");
        currentIndex = getIntent().getIntExtra("current_index", 0);

        if (videoPaths == null || videoPaths.isEmpty()) {
            Toast.makeText(this, R.string.error_playing_video, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        setupGestures();
        resumePlaybackPosition();
    }

    private void initViews() {
        playerRoot = findViewById(R.id.playerRoot);
        surfaceView = findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        controlsOverlay = findViewById(R.id.controlsOverlay);
        topBar = findViewById(R.id.topBar);
        bottomBar = findViewById(R.id.bottomBar);
        centerControls = findViewById(R.id.centerControls);
        lockOverlay = findViewById(R.id.lockOverlay);

        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnBack = findViewById(R.id.btnBack);
        btnFullscreen = findViewById(R.id.btnFullscreen);
        btnLock = findViewById(R.id.btnLock);
        btnUnlock = findViewById(R.id.btnUnlock);
        btnSleepTimer = findViewById(R.id.btnSleepTimer);
        btnVideoInfo = findViewById(R.id.btnVideoInfo);
        btnRewind = findViewById(R.id.btnRewind);
        btnForward = findViewById(R.id.btnForward);
        btnBookmark = findViewById(R.id.btnBookmark);
        btnNote = findViewById(R.id.btnNote);

        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvVideoTitle = findViewById(R.id.tvVideoTitle);
        tvSleepTimer = findViewById(R.id.tvSleepTimer);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvQualityBadge = findViewById(R.id.tvQualityBadge);
        btnSpeed = findViewById(R.id.btnSpeed);
        btnRepeat = findViewById(R.id.btnRepeat);
        btnSubtitle = findViewById(R.id.btnSubtitle);
        seekBar = findViewById(R.id.seekBar);

        brightnessOverlay = findViewById(R.id.brightnessOverlay);
        brightnessProgress = findViewById(R.id.brightnessProgress);
        tvBrightnessValue = findViewById(R.id.tvBrightnessValue);

        volumeOverlay = findViewById(R.id.volumeOverlay);
        volumeProgress = findViewById(R.id.volumeProgress);
        tvVolumeValue = findViewById(R.id.tvVolumeValue);

        seekOverlay = findViewById(R.id.seekOverlay);
        tvSeekPosition = findViewById(R.id.tvSeekPosition);
        tvSeekOffset = findViewById(R.id.tvSeekOffset);

        doubleTapLeft = findViewById(R.id.doubleTapLeft);
        doubleTapRight = findViewById(R.id.doubleTapRight);

        updateSpeedButtonText();
        updateRepeatButtonText();
        updateSubtitleButtonState();
        updateQualityBadge(0, 0);
        updateFullscreenButtonIcon();
    }

    private void setupListeners() {
        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayPause();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrevious();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnFullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFullscreen();
            }
        });

        btnLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLock();
            }
        });

        btnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLock();
            }
        });

        btnSleepTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSleepTimerDialog();
            }
        });

        btnVideoInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVideoInfo();
            }
        });

        btnRewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPrepared) return;
                seekBy(-SEEK_SKIP_TIME);
                resetHideControlsTimer();
            }
        });

        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPrepared) return;
                seekBy(SEEK_SKIP_TIME);
                resetHideControlsTimer();
            }
        });

        btnSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSpeedDialog();
            }
        });

        btnBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBookmarkOptions();
            }
        });

        btnNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNoteOptions();
            }
        });

        btnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cycleRepeatMode();
            }
        });

        btnSubtitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSubtitleButtonClick();
            }
        });

        btnSubtitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showSubtitleOptionsDialog();
                return true;
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isPrepared) {
                    tvCurrentTime.setText(VideoUtils.formatDuration(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(hideControlsRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isPrepared && mediaPlayer != null) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                    updateProgress();
                }
                resetHideControlsTimer();
            }
        });

        updateProgressRunnable = new Runnable() {
            @Override
            public void run() {
                updateProgress();
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    handler.postDelayed(this, UPDATE_PROGRESS_DELAY);
                }
            }
        };

        hideControlsRunnable = new Runnable() {
            @Override
            public void run() {
                hideControls();
            }
        };

        hideGesturesRunnable = new Runnable() {
            @Override
            public void run() {
                brightnessOverlay.setVisibility(View.GONE);
                volumeOverlay.setVisibility(View.GONE);
                seekOverlay.setVisibility(View.GONE);
                doubleTapLeft.setVisibility(View.GONE);
                doubleTapRight.setVisibility(View.GONE);
            }
        };
    }

    private void setupGestures() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (isLocked) {
                    toggleLockVisibility();
                } else {
                    toggleControls();
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isLocked || !isPrepared) return true;

                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                if (e.getX() < screenWidth / 2f) {
                    seekBy(-SEEK_SKIP_TIME);
                    showDoubleTapAnimation(true);
                } else {
                    seekBy(SEEK_SKIP_TIME);
                    showDoubleTapAnimation(false);
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (isLocked || !isPrepared || e1 == null || e2 == null) return false;

                float deltaX = e2.getX() - e1.getX();
                float deltaY = e2.getY() - e1.getY();

                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    if (Math.abs(deltaX) > 50) {
                        handleSeekScroll(deltaX);
                    }
                } else {
                    int screenWidth = getResources().getDisplayMetrics().widthPixels;
                    if (Math.abs(deltaY) > 50) {
                        if (e1.getX() < screenWidth / 2f) {
                            handleBrightnessScroll(deltaY);
                        } else {
                            handleVolumeScroll(deltaY);
                        }
                    }
                }
                return true;
            }
        });

        playerRoot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    seekStartPos = -1;
                    startVolume = -1;
                    currentBrightness = -1.0f;

                    if (seekOverlay.getVisibility() == View.VISIBLE && isPrepared && mediaPlayer != null) {
                        String[] parts = tvSeekPosition.getText().toString().split(":");
                        long targetMs = 0;
                        if (parts.length == 3) {
                            targetMs = (Long.parseLong(parts[0]) * 3600L
                                    + Long.parseLong(parts[1]) * 60L
                                    + Long.parseLong(parts[2])) * 1000L;
                        } else if (parts.length == 2) {
                            targetMs = (Long.parseLong(parts[0]) * 60L
                                    + Long.parseLong(parts[1])) * 1000L;
                        }

                        if (targetMs >= 0 && targetMs <= mediaPlayer.getDuration()) {
                            mediaPlayer.seekTo((int) targetMs);
                        }
                    }

                    handler.removeCallbacks(hideGesturesRunnable);
                    handler.postDelayed(hideGesturesRunnable, 1000);
                }
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    private void handleBrightnessScroll(float deltaY) {
        if (currentBrightness == -1.0f) {
            Window window = getWindow();
            currentBrightness = window.getAttributes().screenBrightness;
            if (currentBrightness < 0) {
                currentBrightness = 0.5f;
            }
        }

        float height = getResources().getDisplayMetrics().heightPixels;
        float change = -(deltaY / height) * 2f;

        float newBrightness = currentBrightness + change;
        newBrightness = Math.max(0.01f, Math.min(1.0f, newBrightness));

        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.screenBrightness = newBrightness;
        window.setAttributes(layoutParams);

        currentBrightness = newBrightness;

        int percent = (int) (newBrightness * 100);
        brightnessOverlay.setVisibility(View.VISIBLE);
        brightnessProgress.setProgress(percent);
        tvBrightnessValue.setText(percent + "%");
    }

    private void handleVolumeScroll(float deltaY) {
        if (startVolume == -1) {
            startVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }

        float height = getResources().getDisplayMetrics().heightPixels;
        float change = -(deltaY / height) * maxVolume * 2f;

        int newVolume = startVolume + (int) change;
        newVolume = Math.max(0, Math.min(maxVolume, newVolume));

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
        startVolume = newVolume;

        int percent = (int) (((float) newVolume / maxVolume) * 100);
        volumeOverlay.setVisibility(View.VISIBLE);
        volumeProgress.setProgress(percent);
        tvVolumeValue.setText(percent + "%");
    }

    private void handleSeekScroll(float deltaX) {
        if (mediaPlayer == null) return;

        if (seekStartPos == -1) {
            seekStartPos = mediaPlayer.getCurrentPosition();
            handler.removeCallbacks(updateProgressRunnable);
        }

        float width = getResources().getDisplayMetrics().widthPixels;
        long duration = mediaPlayer.getDuration();
        long maxSeekChange = 5 * 60 * 1000L;
        long change = (long) ((deltaX / width) * maxSeekChange);

        long targetPos = seekStartPos + change;
        targetPos = Math.max(0, Math.min(duration, targetPos));

        seekOverlay.setVisibility(View.VISIBLE);
        tvSeekPosition.setText(VideoUtils.formatDuration(targetPos));

        int offsetSeconds = (int) (change / 1000);
        String offsetStr = (offsetSeconds > 0 ? "+" : "") + offsetSeconds + " sec";
        tvSeekOffset.setText(offsetStr);
    }

    private void seekBy(int ms) {
        if (mediaPlayer == null) return;
        int target = mediaPlayer.getCurrentPosition() + ms;
        target = Math.max(0, Math.min(mediaPlayer.getDuration(), target));
        mediaPlayer.seekTo(target);
        updateProgress();
    }

    private void showDoubleTapAnimation(boolean left) {
        View overlay = left ? doubleTapLeft : doubleTapRight;
        overlay.setVisibility(View.VISIBLE);
        handler.removeCallbacks(hideGesturesRunnable);
        handler.postDelayed(hideGesturesRunnable, 800);
    }

    private void showSpeedDialog() {
        final String[] speedLabels = {"0.5x", "0.75x", "1.0x", "1.25x", "1.5x", "2.0x"};
        final float[] speedValues = {0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f};

        int checkedIndex = 2;
        for (int i = 0; i < speedValues.length; i++) {
            if (Math.abs(speedValues[i] - currentPlaybackSpeed) < 0.01f) {
                checkedIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.playback_speed)
                .setSingleChoiceItems(speedLabels, checkedIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        applyPlaybackSpeed(speedValues[which], speedLabels[which]);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

        resetHideControlsTimer();
    }

    private void applyPlaybackSpeed(float speed, String speedLabel) {
        currentPlaybackSpeed = speed;
        updateSpeedButtonText();

        if (mediaPlayer == null || !isPrepared) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
            Toast.makeText(this, getString(R.string.speed_changed, speedLabel), Toast.LENGTH_SHORT).show();
        } else {
            currentPlaybackSpeed = 1.0f;
            updateSpeedButtonText();
            Toast.makeText(this, R.string.speed_not_supported, Toast.LENGTH_SHORT).show();
        }
    }

    private void showBookmarkOptions() {
        if (videoIds == null || currentIndex >= videoIds.length) {
            Toast.makeText(this, R.string.error_playing_video, Toast.LENGTH_SHORT).show();
            return;
        }

        final String[] options = {
                getString(R.string.bookmark),
                getString(R.string.view_bookmarks)
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.bookmark)
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            addCurrentBookmark();
                        } else {
                            showBookmarksDialog();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

        resetHideControlsTimer();
    }

    private void addCurrentBookmark() {
        if (!isPrepared || mediaPlayer == null || videoIds == null || currentIndex >= videoIds.length) {
            return;
        }

        long videoId = videoIds[currentIndex];
        long positionMs = mediaPlayer.getCurrentPosition();
        String label = VideoUtils.formatDuration(positionMs);

        VideoDBHelper db = VideoDBHelper.getInstance(this);
        db.addBookmark(videoId, positionMs, label);

        Toast.makeText(this, R.string.bookmark_added, Toast.LENGTH_SHORT).show();
        resetHideControlsTimer();
    }

    private void showBookmarksDialog() {
        if (videoIds == null || currentIndex >= videoIds.length) {
            Toast.makeText(this, R.string.error_playing_video, Toast.LENGTH_SHORT).show();
            return;
        }

        long videoId = videoIds[currentIndex];
        VideoDBHelper db = VideoDBHelper.getInstance(this);
        final List<VideoDBHelper.BookmarkItem> bookmarks = db.getBookmarksForVideo(videoId);

        if (bookmarks.isEmpty()) {
            Toast.makeText(this, R.string.no_bookmarks, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] items = new String[bookmarks.size()];
        for (int i = 0; i < bookmarks.size(); i++) {
            VideoDBHelper.BookmarkItem item = bookmarks.get(i);
            String time = VideoUtils.formatDuration(item.positionMs);
            String label = item.label != null && !item.label.trim().isEmpty() ? item.label : time;
            items[i] = time.equals(label) ? time : time + " - " + label;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.view_bookmarks)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showBookmarkActionDialog(bookmarks.get(which));
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

        resetHideControlsTimer();
    }

    private void showBookmarkActionDialog(final VideoDBHelper.BookmarkItem bookmark) {
        final String[] actions = {
                getString(R.string.go_to_bookmark),
                getString(R.string.delete_bookmark)
        };

        new AlertDialog.Builder(this)
                .setTitle(VideoUtils.formatDuration(bookmark.positionMs))
                .setItems(actions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        VideoDBHelper db = VideoDBHelper.getInstance(VideoPlayerActivity.this);

                        if (which == 0) {
                            if (isPrepared && mediaPlayer != null) {
                                mediaPlayer.seekTo((int) bookmark.positionMs);
                                updateProgress();
                                Toast.makeText(VideoPlayerActivity.this,
                                        R.string.go_to_bookmark,
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            db.deleteBookmark(bookmark.id);
                            Toast.makeText(VideoPlayerActivity.this,
                                    R.string.delete_bookmark,
                                    Toast.LENGTH_SHORT).show();
                            showBookmarksDialog();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

        resetHideControlsTimer();
    }

    private void showNoteOptions() {
        if (videoIds == null || currentIndex >= videoIds.length) {
            Toast.makeText(this, R.string.error_playing_video, Toast.LENGTH_SHORT).show();
            return;
        }

        final String[] options = {
                getString(R.string.add_note),
                getString(R.string.view_notes)
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.notes)
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            showAddNoteDialog();
                        } else {
                            showNotesDialog();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

        resetHideControlsTimer();
    }

    private void showAddNoteDialog() {
        if (!isPrepared || mediaPlayer == null || videoIds == null || currentIndex >= videoIds.length) {
            return;
        }

        final EditText input = new EditText(this);
        input.setHint(R.string.note_hint);
        input.setMinLines(3);
        input.setMaxLines(5);

        new AlertDialog.Builder(this)
                .setTitle(R.string.add_note)
                .setView(input)
                .setPositiveButton(R.string.add_note, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String content = input.getText().toString().trim();
                        if (content.isEmpty()) {
                            return;
                        }

                        long videoId = videoIds[currentIndex];
                        long positionMs = mediaPlayer.getCurrentPosition();
                        VideoDBHelper db = VideoDBHelper.getInstance(VideoPlayerActivity.this);
                        db.addNote(videoId, positionMs, content);

                        Toast.makeText(VideoPlayerActivity.this,
                                R.string.note_saved,
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

        resetHideControlsTimer();
    }

    private void showNotesDialog() {
        if (videoIds == null || currentIndex >= videoIds.length) {
            Toast.makeText(this, R.string.error_playing_video, Toast.LENGTH_SHORT).show();
            return;
        }

        long videoId = videoIds[currentIndex];
        VideoDBHelper db = VideoDBHelper.getInstance(this);
        final List<VideoDBHelper.NoteItem> notes = db.getNotesForVideo(videoId);

        if (notes.isEmpty()) {
            Toast.makeText(this, R.string.no_notes, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] items = new String[notes.size()];
        for (int i = 0; i < notes.size(); i++) {
            VideoDBHelper.NoteItem item = notes.get(i);
            String time = VideoUtils.formatDuration(item.positionMs);
            String content = item.content == null ? "" : item.content.trim();
            items[i] = time + " - " + content;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.view_notes)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showNoteActionDialog(notes.get(which));
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

        resetHideControlsTimer();
    }

    private void showNoteActionDialog(final VideoDBHelper.NoteItem note) {
        final String[] actions = {
                getString(R.string.go_to_note),
                getString(R.string.delete_note)
        };

        new AlertDialog.Builder(this)
                .setTitle(VideoUtils.formatDuration(note.positionMs))
                .setMessage(note.content)
                .setItems(actions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        VideoDBHelper db = VideoDBHelper.getInstance(VideoPlayerActivity.this);

                        if (which == 0) {
                            if (isPrepared && mediaPlayer != null) {
                                mediaPlayer.seekTo((int) note.positionMs);
                                updateProgress();
                                Toast.makeText(VideoPlayerActivity.this,
                                        R.string.go_to_note,
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            db.deleteNote(note.id);
                            Toast.makeText(VideoPlayerActivity.this,
                                    R.string.delete_note,
                                    Toast.LENGTH_SHORT).show();
                            showNotesDialog();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

        resetHideControlsTimer();
    }

    private void updateSpeedButtonText() {
        if (btnSpeed != null) {
            btnSpeed.setText(String.format(java.util.Locale.US, "%.2fx", currentPlaybackSpeed)
                    .replace(".00x", ".0x"));
        }
    }

    private void updateRepeatButtonText() {
        if (btnRepeat == null) {
            return;
        }

        switch (repeatMode) {
            case REPEAT_MODE_ONE:
                btnRepeat.setText(R.string.repeat_mode_one_short);
                break;
            case REPEAT_MODE_ALL:
                btnRepeat.setText(R.string.repeat_mode_all_short);
                break;
            case REPEAT_MODE_OFF:
            default:
                btnRepeat.setText(R.string.repeat_mode_off_short);
                break;
        }
    }

    private void cycleRepeatMode() {
        repeatMode = (repeatMode + 1) % 3;
        updateRepeatButtonText();

        int messageResId;
        if (repeatMode == REPEAT_MODE_ONE) {
            messageResId = R.string.repeat_mode_one_label;
        } else if (repeatMode == REPEAT_MODE_ALL) {
            messageResId = R.string.repeat_mode_all_label;
        } else {
            messageResId = R.string.repeat_mode_off_label;
        }

        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
        resetHideControlsTimer();
    }

    private void updateSubtitleButtonState() {
        if (btnSubtitle == null) {
            return;
        }

        String currentVideoPath = getCurrentVideoPath();
        boolean isGeneratingForCurrentVideo = isGeneratingAiSubtitles
                && currentVideoPath != null
                && currentVideoPath.equals(generatingSubtitleVideoPath);

        if (isGeneratingForCurrentVideo) {
            btnSubtitle.setText(R.string.subtitle_generating_short);
            btnSubtitle.setAlpha(1f);
            return;
        }

        if (!subtitleFileAvailable) {
            boolean localVideo = isLocalVideoPath(currentVideoPath);
            btnSubtitle.setText(localVideo
                    ? R.string.subtitle_generate_ai_short
                    : R.string.subtitle_unavailable_short);
            btnSubtitle.setAlpha(localVideo ? 1f : 0.5f);
            return;
        }

        btnSubtitle.setAlpha(1f);
        btnSubtitle.setText(subtitlesEnabled
                ? R.string.subtitle_on_short
                : R.string.subtitle_off_short);
    }

    private void handleSubtitleButtonClick() {
        if (subtitleFileAvailable) {
            toggleSubtitles();
        } else {
            showSubtitleOptionsDialog();
        }
    }

    private void showSubtitleOptionsDialog() {
        final String videoPath = getCurrentVideoPath();
        if (videoPath == null) {
            Toast.makeText(this, R.string.error_playing_video, Toast.LENGTH_SHORT).show();
            return;
        }

        final boolean localVideo = isLocalVideoPath(videoPath);
        if (!localVideo && !subtitleFileAvailable) {
            Toast.makeText(this, R.string.ai_subtitle_local_only, Toast.LENGTH_SHORT).show();
            return;
        }

        final boolean hasApiKey = AiSubtitleGenerator.hasApiKey(this);
        final String primaryAction = getString(subtitleFileAvailable
                ? R.string.regenerate_ai_subtitles
                : (hasApiKey ? R.string.generate_ai_subtitles : R.string.set_ai_api_key_and_generate));

        final int actionToggle = 1;
        final int actionViewList = 2;
        final int actionPrimary = 3;
        final int actionSetApiKey = 4;
        final int actionClearApiKey = 5;

        final List<String> options = new ArrayList<>();
        final List<Integer> actionIds = new ArrayList<>();

        if (subtitleFileAvailable && !subtitleCues.isEmpty()) {
            options.add(getString(subtitlesEnabled
                    ? R.string.turn_subtitles_off
                    : R.string.turn_subtitles_on));
            actionIds.add(actionToggle);

            options.add(getString(R.string.view_subtitle_list));
            actionIds.add(actionViewList);
        }

        if (localVideo) {
            options.add(primaryAction);
            actionIds.add(actionPrimary);

            options.add(getString(R.string.set_ai_api_key));
            actionIds.add(actionSetApiKey);

            if (hasApiKey) {
                options.add(getString(R.string.clear_ai_api_key));
                actionIds.add(actionClearApiKey);
            }
        }

        final String[] optionItems = options.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle(R.string.subtitle_options)
                .setItems(optionItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which < 0 || which >= actionIds.size()) {
                            return;
                        }

                        int action = actionIds.get(which);
                        if (action == actionToggle) {
                            toggleSubtitles();
                        } else if (action == actionViewList) {
                            showSubtitleBrowserDialog();
                        } else if (action == actionPrimary) {
                            if (hasApiKey) {
                                generateAiSubtitles();
                            } else {
                                showAiApiKeyDialog(true);
                            }
                        } else if (action == actionSetApiKey) {
                            showAiApiKeyDialog(false);
                        } else if (action == actionClearApiKey) {
                            AiSubtitleGenerator.saveApiKey(VideoPlayerActivity.this, "");
                            Toast.makeText(VideoPlayerActivity.this,
                                    R.string.ai_api_key_cleared,
                                    Toast.LENGTH_SHORT).show();
                            updateSubtitleButtonState();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

        resetHideControlsTimer();
    }

    private void showSubtitleBrowserDialog() {
        if (!subtitleFileAvailable || subtitleCues.isEmpty()) {
            Toast.makeText(this, R.string.subtitles_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] items = new String[subtitleCues.size()];
        for (int i = 0; i < subtitleCues.size(); i++) {
            SubtitleCue cue = subtitleCues.get(i);
            String preview = cue.text == null ? "" : cue.text.replace('\n', ' ').trim();
            if (preview.length() > 64) {
                preview = preview.substring(0, 61) + "...";
            }
            items[i] = VideoUtils.formatDuration(cue.startMs) + "  " + preview;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.subtitle_list_title)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which < 0 || which >= subtitleCues.size()) {
                            return;
                        }

                        SubtitleCue cue = subtitleCues.get(which);
                        subtitlesEnabled = true;
                        updateSubtitleButtonState();

                        if (mediaPlayer != null && isPrepared) {
                            mediaPlayer.seekTo((int) cue.startMs);
                            updateSubtitleForPosition((int) cue.startMs);
                        }

                        Toast.makeText(VideoPlayerActivity.this,
                                getString(R.string.subtitle_seek_to,
                                        VideoUtils.formatDuration(cue.startMs)),
                                Toast.LENGTH_SHORT).show();
                        resetHideControlsTimer();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

        resetHideControlsTimer();
    }

    private void showAiApiKeyDialog(final boolean generateAfterSave) {
        final EditText input = new EditText(this);
        input.setHint(R.string.ai_api_key_hint);

        String currentApiKey = AiSubtitleGenerator.getApiKey(this);
        if (currentApiKey != null && !currentApiKey.trim().isEmpty()) {
            input.setText(currentApiKey);
            input.setSelection(currentApiKey.length());
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.ai_api_key_title)
                .setView(input)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String apiKey = input.getText() == null
                                ? ""
                                : input.getText().toString().trim();

                        if (apiKey.isEmpty()) {
                            Toast.makeText(VideoPlayerActivity.this,
                                    R.string.ai_api_key_required,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        AiSubtitleGenerator.saveApiKey(VideoPlayerActivity.this, apiKey);
                        Toast.makeText(VideoPlayerActivity.this,
                                R.string.ai_api_key_saved,
                                Toast.LENGTH_SHORT).show();
                        updateSubtitleButtonState();

                        if (generateAfterSave) {
                            generateAiSubtitles();
                        }
                    }
                })
                .setNeutralButton(R.string.clear_ai_api_key, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AiSubtitleGenerator.saveApiKey(VideoPlayerActivity.this, "");
                        Toast.makeText(VideoPlayerActivity.this,
                                R.string.ai_api_key_cleared,
                                Toast.LENGTH_SHORT).show();
                        updateSubtitleButtonState();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

        resetHideControlsTimer();
    }

    private void generateAiSubtitles() {
        final String videoPath = getCurrentVideoPath();
        if (videoPath == null) {
            Toast.makeText(this, R.string.error_playing_video, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isLocalVideoPath(videoPath)) {
            Toast.makeText(this, R.string.ai_subtitle_local_only, Toast.LENGTH_SHORT).show();
            return;
        }

        if (isGeneratingAiSubtitles) {
            Toast.makeText(this, R.string.ai_subtitle_generating, Toast.LENGTH_SHORT).show();
            return;
        }

        isGeneratingAiSubtitles = true;
        generatingSubtitleVideoPath = videoPath;
        subtitlesEnabled = false;
        updateSubtitleButtonState();
        hideSubtitleOverlay();

        AiSubtitleGenerator.generateSubtitles(this, videoPath, new AiSubtitleGenerator.Callback() {
            @Override
            public void onStarted() {
                Toast.makeText(VideoPlayerActivity.this,
                        R.string.ai_subtitle_generating,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(File subtitleFile) {
                isGeneratingAiSubtitles = false;
                generatingSubtitleVideoPath = null;

                String currentVideoPath = getCurrentVideoPath();
                if (videoPath.equals(currentVideoPath)) {
                    subtitlesEnabled = true;
                    prepareSubtitleTrack(videoPath);
                    updateSubtitleButtonState();

                    if (mediaPlayer != null && isPrepared) {
                        updateSubtitleForPosition(mediaPlayer.getCurrentPosition());
                    }
                } else {
                    updateSubtitleButtonState();
                }

                Toast.makeText(VideoPlayerActivity.this,
                        R.string.ai_subtitle_generated,
                        Toast.LENGTH_SHORT).show();
                resetHideControlsTimer();
            }

            @Override
            public void onError(String message) {
                isGeneratingAiSubtitles = false;
                generatingSubtitleVideoPath = null;
                updateSubtitleButtonState();
                Toast.makeText(VideoPlayerActivity.this,
                        getString(R.string.ai_subtitle_generation_failed, message),
                        Toast.LENGTH_LONG).show();
                resetHideControlsTimer();
            }
        });
    }

    private String getCurrentVideoPath() {
        if (videoPaths == null || currentIndex < 0 || currentIndex >= videoPaths.size()) {
            return null;
        }
        return videoPaths.get(currentIndex);
    }

    private boolean isLocalVideoPath(String videoPath) {
        return videoPath != null
                && !videoPath.startsWith("http://")
                && !videoPath.startsWith("https://");
    }

    private void toggleSubtitles() {
        if (!subtitleFileAvailable) {
            showSubtitleOptionsDialog();
            return;
        }

        subtitlesEnabled = !subtitlesEnabled;
        updateSubtitleButtonState();

        if (subtitlesEnabled && mediaPlayer != null && isPrepared) {
            updateSubtitleForPosition(mediaPlayer.getCurrentPosition());
            Toast.makeText(this, R.string.subtitles_on, Toast.LENGTH_SHORT).show();
        } else {
            hideSubtitleOverlay();
            Toast.makeText(this, R.string.subtitles_off, Toast.LENGTH_SHORT).show();
        }

        resetHideControlsTimer();
    }

    private void prepareSubtitleTrack(String videoPath) {
        subtitleCues.clear();
        subtitleFileAvailable = false;
        currentSubtitleIndex = -1;
        hideSubtitleOverlay();

        File subtitleFile = findSubtitleFile(videoPath);
        if (subtitleFile != null) {
            loadSubtitleCues(subtitleFile);
        }

        updateSubtitleButtonState();
    }

    private File findSubtitleFile(String videoPath) {
        if (videoPath == null) {
            return null;
        }

        File generatedFile = AiSubtitleGenerator.getGeneratedSubtitleFile(this, videoPath);
        if (generatedFile.exists()) {
            return generatedFile;
        }

        if (videoPath.startsWith("http://") || videoPath.startsWith("https://")) {
            return null;
        }

        File videoFile = new File(videoPath);
        File parent = videoFile.getParentFile();
        if (parent == null) {
            return null;
        }

        String fileName = videoFile.getName();
        int extensionIndex = fileName.lastIndexOf('.');
        String baseName = extensionIndex >= 0 ? fileName.substring(0, extensionIndex) : fileName;

        File lowerCaseFile = new File(parent, baseName + ".srt");
        if (lowerCaseFile.exists()) {
            return lowerCaseFile;
        }

        File upperCaseFile = new File(parent, baseName + ".SRT");
        if (upperCaseFile.exists()) {
            return upperCaseFile;
        }

        return null;
    }

    private void loadSubtitleCues(File subtitleFile) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(subtitleFile), "UTF-8"))) {
            List<String> blockLines = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }

                if (line.trim().isEmpty()) {
                    addSubtitleCueBlock(blockLines);
                    blockLines.clear();
                } else {
                    blockLines.add(line);
                }
            }

            addSubtitleCueBlock(blockLines);
            subtitleFileAvailable = !subtitleCues.isEmpty();
        } catch (IOException e) {
            subtitleCues.clear();
            subtitleFileAvailable = false;
        }
    }

    private void addSubtitleCueBlock(List<String> blockLines) {
        if (blockLines == null || blockLines.isEmpty()) {
            return;
        }

        int timeLineIndex = -1;
        for (int i = 0; i < blockLines.size(); i++) {
            if (blockLines.get(i).contains("-->")) {
                timeLineIndex = i;
                break;
            }
        }

        if (timeLineIndex == -1) {
            return;
        }

        String[] timeRange = blockLines.get(timeLineIndex).split("-->");
        if (timeRange.length != 2) {
            return;
        }

        long startMs = parseSubtitleTimeToMs(timeRange[0]);
        long endMs = parseSubtitleTimeToMs(timeRange[1]);
        if (startMs < 0 || endMs <= startMs) {
            return;
        }

        StringBuilder textBuilder = new StringBuilder();
        for (int i = timeLineIndex + 1; i < blockLines.size(); i++) {
            String textLine = blockLines.get(i).trim();
            if (textLine.isEmpty()) {
                continue;
            }
            if (textBuilder.length() > 0) {
                textBuilder.append('\n');
            }
            textBuilder.append(textLine);
        }

        if (textBuilder.length() == 0) {
            return;
        }

        subtitleCues.add(new SubtitleCue(startMs, endMs, textBuilder.toString()));
    }

    private long parseSubtitleTimeToMs(String value) {
        if (value == null) {
            return -1;
        }

        String normalized = value.trim().replace('.', ',');
        String[] timeAndMs = normalized.split(",");
        if (timeAndMs.length != 2) {
            return -1;
        }

        String[] hms = timeAndMs[0].trim().split(":");
        if (hms.length != 3) {
            return -1;
        }

        try {
            long hours = Long.parseLong(hms[0].trim());
            long minutes = Long.parseLong(hms[1].trim());
            long seconds = Long.parseLong(hms[2].trim());
            long millis = Long.parseLong(timeAndMs[1].trim());

            return hours * 3600000L + minutes * 60000L + seconds * 1000L + millis;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void updateSubtitleForPosition(int positionMs) {
        if (!subtitlesEnabled || !subtitleFileAvailable || subtitleCues.isEmpty()) {
            hideSubtitleOverlay();
            return;
        }

        for (int i = 0; i < subtitleCues.size(); i++) {
            SubtitleCue cue = subtitleCues.get(i);
            if (positionMs >= cue.startMs && positionMs <= cue.endMs) {
                if (currentSubtitleIndex != i) {
                    currentSubtitleIndex = i;
                    tvSubtitle.setText(cue.text);
                    tvSubtitle.setVisibility(View.VISIBLE);
                }
                return;
            }

            if (positionMs < cue.startMs) {
                break;
            }
        }

        hideSubtitleOverlay();
    }

    private void hideSubtitleOverlay() {
        currentSubtitleIndex = -1;
        if (tvSubtitle != null) {
            tvSubtitle.setText("");
            tvSubtitle.setVisibility(View.GONE);
        }
    }

    private void updateQualityBadge(int width, int height) {
        if (tvQualityBadge != null) {
            tvQualityBadge.setText(getQualityLabel(width, height));
        }
    }

    private String getQualityLabel(int width, int height) {
        if (width <= 0 || height <= 0) {
            return getString(R.string.quality_unknown);
        }

        int largerSide = Math.max(width, height);
        if (largerSide >= 3840) {
            return getString(R.string.quality_4k);
        }
        if (largerSide >= 2560) {
            return getString(R.string.quality_2k);
        }
        if (largerSide >= 1920) {
            return getString(R.string.quality_fhd);
        }
        if (largerSide >= 1280) {
            return getString(R.string.quality_hd);
        }
        if (largerSide >= 854) {
            return getString(R.string.quality_sd);
        }
        return getString(R.string.quality_low);
    }

    private void initMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnVideoSizeChangedListener(this);
        } else {
            mediaPlayer.reset();
        }
        isPrepared = false;
    }

    private void loadVideo() {
        if (currentIndex < 0 || currentIndex >= videoPaths.size()) {
            finish();
            return;
        }
        hasHandledResumePrompt = false;

        String path = videoPaths.get(currentIndex);
        String title = videoTitles != null && currentIndex < videoTitles.size()
                ? videoTitles.get(currentIndex)
                : "Unknown Video";
        tvVideoTitle.setText(title != null ? title : "Unknown Video");
        prepareSubtitleTrack(path);
        updateQualityBadge(0, 0);

        try {
            initMediaPlayer();
            mediaPlayer.setDataSource(path);
            if (isSurfaceCreated) {
                mediaPlayer.setDisplay(surfaceHolder);
            }
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Toast.makeText(this, R.string.error_playing_video, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepared = true;

        int duration = mp.getDuration();
        seekBar.setMax(duration);
        tvTotalTime.setText(VideoUtils.formatDuration(duration));

        adjustAspectRatio(mp.getVideoWidth(), mp.getVideoHeight());
        updateQualityBadge(mp.getVideoWidth(), mp.getVideoHeight());

        if (videoIds != null && currentIndex < videoIds.length) {
            long videoId = videoIds[currentIndex];
            VideoDBHelper db = VideoDBHelper.getInstance(this);
            long savedPosition = db.getWatchPosition(videoId);
            if (savedPosition >= RESUME_PROMPT_THRESHOLD_MS
                    && savedPosition < duration - RESUME_PROMPT_THRESHOLD_MS
                    && !hasHandledResumePrompt) {
                hasHandledResumePrompt = true;
                showResumePrompt(savedPosition);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && currentPlaybackSpeed != 1.0f) {
            mp.setPlaybackParams(mp.getPlaybackParams().setSpeed(currentPlaybackSpeed));
        }
        updateSpeedButtonText();
        updateSubtitleButtonState();

        mp.start();
        btnPlayPause.setImageResource(R.drawable.ic_pause);
        updateProgress();
        handler.post(updateProgressRunnable);
        resetHideControlsTimer();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        handler.removeCallbacks(updateProgressRunnable);
        saveWatchPosition(0);
        hideSubtitleOverlay();

        if (repeatMode == REPEAT_MODE_ONE) {
            loadVideo();
            return;
        }

        if (currentIndex < videoPaths.size() - 1) {
            currentIndex++;
            loadVideo();
            return;
        }

        if (repeatMode == REPEAT_MODE_ALL && !videoPaths.isEmpty()) {
            currentIndex = 0;
            loadVideo();
            return;
        }

        btnPlayPause.setImageResource(R.drawable.ic_play);
        showControls();
        updateProgress();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(this, R.string.error_playing_video, Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        adjustAspectRatio(width, height);
        updateQualityBadge(width, height);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isSurfaceCreated = true;
        if (mediaPlayer != null) {
            mediaPlayer.setDisplay(holder);
        } else {
            loadVideo();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isSurfaceCreated = false;
        if (mediaPlayer != null) {
            mediaPlayer.setDisplay(null);
        }
    }

    private void togglePlayPause() {
        if (!isPrepared || mediaPlayer == null) return;

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayPause.setImageResource(R.drawable.ic_play);
            handler.removeCallbacks(updateProgressRunnable);
            showControls();
            handler.removeCallbacks(hideControlsRunnable);
        } else {
            mediaPlayer.start();
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            handler.post(updateProgressRunnable);
            resetHideControlsTimer();
        }
    }

    private void playNext() {
        if (videoPaths == null || videoPaths.isEmpty()) {
            return;
        }

        if (mediaPlayer != null && isPrepared) {
            saveWatchPosition(mediaPlayer.getCurrentPosition());
        }

        if (currentIndex < videoPaths.size() - 1) {
            currentIndex++;
            loadVideo();
        } else if (repeatMode == REPEAT_MODE_ALL && videoPaths.size() > 1) {
            currentIndex = 0;
            loadVideo();
        } else {
            Toast.makeText(this, R.string.last_video, Toast.LENGTH_SHORT).show();
        }
    }

    private void playPrevious() {
        if (videoPaths == null || videoPaths.isEmpty()) {
            return;
        }

        if (mediaPlayer != null && isPrepared) {
            saveWatchPosition(mediaPlayer.getCurrentPosition());
        }

        if (currentIndex > 0) {
            currentIndex--;
            loadVideo();
        } else if (repeatMode == REPEAT_MODE_ALL && videoPaths.size() > 1) {
            currentIndex = videoPaths.size() - 1;
            loadVideo();
        } else {
            Toast.makeText(this, R.string.first_video, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProgress() {
        if (mediaPlayer != null && isPrepared) {
            int currentPos = mediaPlayer.getCurrentPosition();
            seekBar.setProgress(currentPos);
            tvCurrentTime.setText(VideoUtils.formatDuration(currentPos));
            updateSubtitleForPosition(currentPos);
        } else {
            hideSubtitleOverlay();
        }
    }

    private void adjustAspectRatio(int videoWidth, int videoHeight) {
        if (videoWidth == 0 || videoHeight == 0) return;

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        float videoProportion = (float) videoWidth / (float) videoHeight;
        float screenProportion = (float) screenWidth / (float) screenHeight;

        android.view.ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();

        if (videoProportion > screenProportion) {
            lp.width = screenWidth;
            lp.height = (int) ((float) screenWidth / videoProportion);
        } else {
            lp.width = (int) (videoProportion * (float) screenHeight);
            lp.height = screenHeight;
        }

        surfaceView.setLayoutParams(lp);
    }

    private void toggleControls() {
        if (isControlsVisible) {
            hideControls();
        } else {
            showControls();
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                resetHideControlsTimer();
            }
        }
    }

    private void hideControls() {
        controlsOverlay.setVisibility(View.GONE);
        isControlsVisible = false;
        hideSystemUI();
    }

    private void showControls() {
        controlsOverlay.setVisibility(View.VISIBLE);
        isControlsVisible = true;
        showSystemUI();
        updateProgress();
    }

    private void resetHideControlsTimer() {
        handler.removeCallbacks(hideControlsRunnable);
        handler.postDelayed(hideControlsRunnable, HIDE_CONTROLS_DELAY);
    }

    private void toggleLock() {
        isLocked = !isLocked;
        if (isLocked) {
            controlsOverlay.setVisibility(View.GONE);
            lockOverlay.setVisibility(View.VISIBLE);
            hideSystemUI();
            handler.removeCallbacks(hideControlsRunnable);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    btnUnlock.setVisibility(View.GONE);
                }
            }, 2000);
        } else {
            lockOverlay.setVisibility(View.GONE);
            btnUnlock.setVisibility(View.VISIBLE);
            showControls();
            resetHideControlsTimer();
        }
    }

    private void toggleLockVisibility() {
        if (btnUnlock.getVisibility() == View.VISIBLE) {
            btnUnlock.setVisibility(View.GONE);
        } else {
            btnUnlock.setVisibility(View.VISIBLE);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isLocked) btnUnlock.setVisibility(View.GONE);
                }
            }, 3000);
        }
    }

    private void toggleFullscreen() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
    }

    private void updateFullscreenButtonIcon() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            btnFullscreen.setImageResource(R.drawable.ic_fullscreen_exit);
            isFullscreen = true;
        } else {
            btnFullscreen.setImageResource(R.drawable.ic_fullscreen);
            isFullscreen = false;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateFullscreenButtonIcon();

        if (isPrepared && mediaPlayer != null) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    adjustAspectRatio(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
                }
            }, 100);
        }
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void showSleepTimerDialog() {
        final String[] options = {"Off", "15 Minutes", "30 Minutes", "60 Minutes", "90 Minutes"};
        final int[] values = {0, 15, 30, 60, 90};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sleep_timer);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setSleepTimer(values[which]);
            }
        });
        builder.show();
        resetHideControlsTimer();
    }

    private void setSleepTimer(int minutes) {
        if (sleepTimerRunnable != null) {
            handler.removeCallbacks(sleepTimerRunnable);
        }

        if (minutes == 0) {
            tvSleepTimer.setVisibility(View.GONE);
            Toast.makeText(this, R.string.sleep_timer_off_message, Toast.LENGTH_SHORT).show();
            return;
        }

        sleepTimerMinutes = minutes;
        tvSleepTimer.setVisibility(View.VISIBLE);
        updateSleepTimerDisplay();

        sleepTimerRunnable = new Runnable() {
            @Override
            public void run() {
                sleepTimerMinutes--;
                if (sleepTimerMinutes <= 0) {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    }
                    tvSleepTimer.setVisibility(View.GONE);
                    finish();
                } else {
                    updateSleepTimerDisplay();
                    handler.postDelayed(this, 60000);
                }
            }
        };

        handler.postDelayed(sleepTimerRunnable, 60000);
        Toast.makeText(this, getString(R.string.sleep_timer_set_for, minutes), Toast.LENGTH_SHORT).show();
    }

    private void updateSleepTimerDisplay() {
        tvSleepTimer.setText(sleepTimerMinutes + "m left");
    }

    private void showVideoInfo() {
        if (!isPrepared || mediaPlayer == null) return;

        String path = videoPaths.get(currentIndex);
        int width = mediaPlayer.getVideoWidth();
        int height = mediaPlayer.getVideoHeight();
        String duration = VideoUtils.formatDuration(mediaPlayer.getDuration());

        String info = getString(R.string.info_path) + ": " + path + "\n\n"
                + getString(R.string.info_resolution) + ": " + width + "x" + height + "\n\n"
                + getString(R.string.info_quality) + ": " + getQualityLabel(width, height) + "\n\n"
                + getString(R.string.info_duration) + ": " + duration + "\n\n"
                + getString(R.string.info_subtitles) + ": "
                + getString(subtitleFileAvailable
                ? R.string.info_subtitles_available
                : R.string.info_subtitles_unavailable);

        new AlertDialog.Builder(this)
                .setTitle(R.string.video_info_title)
                .setMessage(info)
                .setPositiveButton(R.string.ok, null)
                .show();

        resetHideControlsTimer();
    }

    private void saveWatchPosition(int position) {
        if (videoIds != null && currentIndex < videoIds.length && isPrepared && mediaPlayer != null) {
            long videoId = videoIds[currentIndex];
            long duration = mediaPlayer.getDuration();

            VideoDBHelper db = VideoDBHelper.getInstance(this);
            db.saveWatchHistory(videoId, position, duration);
        }
    }

    private void showResumePrompt(final long savedPosition) {
        if (mediaPlayer == null || !isPrepared) {
            return;
        }

        final String formattedTime = VideoUtils.formatDuration(savedPosition);

        new AlertDialog.Builder(this)
                .setTitle(R.string.resume_title)
                .setMessage(getString(R.string.resume_message, formattedTime))
                .setPositiveButton(R.string.resume, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mediaPlayer != null && isPrepared) {
                            mediaPlayer.seekTo((int) savedPosition);
                            updateProgress();
                        }
                    }
                })
                .setNegativeButton(R.string.start_over, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mediaPlayer != null && isPrepared) {
                            mediaPlayer.seekTo(0);
                            updateProgress();
                            saveWatchPosition(0);
                        }
                    }
                })
                .setCancelable(true)
                .show();
    }

    private void resumePlaybackPosition() {
        hideSubtitleOverlay();
        updateRepeatButtonText();
        updateSubtitleButtonState();

        if (isSurfaceCreated && mediaPlayer == null) {
            loadVideo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                btnPlayPause.setImageResource(R.drawable.ic_play);
            }
            if (isPrepared) {
                saveWatchPosition(mediaPlayer.getCurrentPosition());
            }
        }
        handler.removeCallbacks(updateProgressRunnable);
        handler.removeCallbacks(hideControlsRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && isPrepared) {
            updateProgress();
            showControls();
        }
        hideSystemUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (isPrepared) {
                saveWatchPosition(mediaPlayer.getCurrentPosition());
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onBackPressed() {
        if (isLocked) {
            Toast.makeText(this, R.string.screen_locked, Toast.LENGTH_SHORT).show();
            toggleLockVisibility();
            return;
        }

        if (isFullscreen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            return;
        }

        super.onBackPressed();
    }
}
