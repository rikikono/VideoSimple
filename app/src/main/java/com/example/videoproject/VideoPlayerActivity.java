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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnVideoSizeChangedListener {

    private static final int HIDE_CONTROLS_DELAY = 3000;
    private static final int UPDATE_PROGRESS_DELAY = 1000;
    private static final int SEEK_SKIP_TIME = 10000;
    private static final int RESUME_PROMPT_THRESHOLD_MS = 5000;

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
    private TextView btnSpeed;
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
        btnSpeed = findViewById(R.id.btnSpeed);
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

        mp.start();
        btnPlayPause.setImageResource(R.drawable.ic_pause);
        handler.post(updateProgressRunnable);
        resetHideControlsTimer();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        btnPlayPause.setImageResource(R.drawable.ic_play);
        handler.removeCallbacks(updateProgressRunnable);
        showControls();
        saveWatchPosition(0);

        if (currentIndex < videoPaths.size() - 1) {
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(this, R.string.error_playing_video, Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        adjustAspectRatio(width, height);
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
        if (currentIndex < videoPaths.size() - 1) {
            if (mediaPlayer != null) {
                saveWatchPosition(mediaPlayer.getCurrentPosition());
            }
            currentIndex++;
            loadVideo();
        } else {
            Toast.makeText(this, "Last video", Toast.LENGTH_SHORT).show();
        }
    }

    private void playPrevious() {
        if (currentIndex > 0) {
            if (mediaPlayer != null) {
                saveWatchPosition(mediaPlayer.getCurrentPosition());
            }
            currentIndex--;
            loadVideo();
        } else {
            Toast.makeText(this, "First video", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProgress() {
        if (mediaPlayer != null && isPrepared) {
            int currentPos = mediaPlayer.getCurrentPosition();
            seekBar.setProgress(currentPos);
            tvCurrentTime.setText(VideoUtils.formatDuration(currentPos));
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
            Toast.makeText(this, "Sleep timer off", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(this, "Sleep timer set for " + minutes + " minutes", Toast.LENGTH_SHORT).show();
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
        String title = videoTitles != null && currentIndex < videoTitles.size()
                ? videoTitles.get(currentIndex)
                : "Unknown Video";

        String info = "Title: " + title + "\n\n" +
                "Path: " + path + "\n\n" +
                "Resolution: " + width + "x" + height + "\n\n" +
                "Duration: " + duration;

        new AlertDialog.Builder(this)
                .setTitle(R.string.video_info)
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
            Toast.makeText(this, "Screen is locked", Toast.LENGTH_SHORT).show();
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
