package com.example.musicproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Video Player Activity handling media playback, SurfaceView, gestures (brightness/volume/seek),
 * sleep timer, and screen orientation.
 */
public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnVideoSizeChangedListener {

    private static final String TAG = "VideoPlayerActivity";
    private static final int HIDE_CONTROLS_DELAY = 3000; // 3 seconds
    private static final int UPDATE_PROGRESS_DELAY = 1000; // 1 second
    private static final int SEEK_SKIP_TIME = 10000; // 10 seconds skip on double tap

    // Media and Surface
    private MediaPlayer mediaPlayer;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    // UI Components
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

    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    private TextView tvVideoTitle;
    private TextView tvSleepTimer;
    private SeekBar seekBar;

    // Gesture Overlay UI
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

    // Data
    private ArrayList<String> videoPaths;
    private ArrayList<String> videoTitles;
    private long[] videoIds;
    private int currentIndex = 0;

    // State
    private boolean isControlsVisible = true;
    private boolean isLocked = false;
    private boolean isFullscreen = false;
    private boolean isPrepared = false;
    private boolean isSurfaceCreated = false;

    // Gesture and Hardware Control
    private GestureDetector gestureDetector;
    private AudioManager audioManager;
    private int maxVolume;
    private float currentBrightness = -1.0f;
    private int startVolume;
    private long seekStartPos;

    // Handlers and Runnables
    private Handler handler = new Handler();
    private Runnable updateProgressRunnable;
    private Runnable hideControlsRunnable;
    private Runnable hideGesturesRunnable;
    private Runnable sleepTimerRunnable;

    // Timers
    private int sleepTimerMinutes = 0; // 0 means off

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set fullscreen flag before content view
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_player);

        // Initialize Audio Manager
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        // Get Intent Data
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

        // Resume playback position if available
        resumePlaybackPosition();
    }

    private void initViews() {
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

        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvVideoTitle = findViewById(R.id.tvVideoTitle);
        tvSleepTimer = findViewById(R.id.tvSleepTimer);
        seekBar = findViewById(R.id.seekBar);

        // Gesture overlays
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

        // Initial UI state update based on orientation
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
                if (isPrepared) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
                resetHideControlsTimer();
            }
        });

        // Runnables
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
                    // Double tap left - Rewind
                    seekBy(-SEEK_SKIP_TIME);
                    showDoubleTapAnimation(true);
                } else {
                    // Double tap right - Forward
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

                // Determine dominant scroll direction
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    // Horizontal scroll - Seeking
                    if (Math.abs(deltaX) > 50) { // Threshold
                        handleSeekScroll(deltaX);
                    }
                } else {
                    // Vertical scroll - Brightness or Volume
                    int screenWidth = getResources().getDisplayMetrics().widthPixels;
                    if (Math.abs(deltaY) > 50) {
                        if (e1.getX() < screenWidth / 2f) {
                            // Left side - Brightness
                            handleBrightnessScroll(deltaY);
                        } else {
                            // Right side - Volume
                            handleVolumeScroll(deltaY);
                        }
                    }
                }
                return true;
            }
        });

        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Reset gesture start states
                    seekStartPos = -1;
                    startVolume = -1;
                    currentBrightness = -1.0f;

                    // Apply seek if we were seeking
                    if (seekOverlay.getVisibility() == View.VISIBLE && isPrepared) {
                        // The text view contains format "00:00:00", we need to parse it or store the target
                        // Better to parse from tvSeekPosition
                        String[] parts = tvSeekPosition.getText().toString().split(":");
                        long targetMs = 0;
                        if (parts.length == 3) {
                            targetMs = (Long.parseLong(parts[0]) * 3600 +
                                    Long.parseLong(parts[1]) * 60 +
                                    Long.parseLong(parts[2])) * 1000;
                        } else if (parts.length == 2) {
                            targetMs = (Long.parseLong(parts[0]) * 60 +
                                    Long.parseLong(parts[1])) * 1000;
                        }

                        if (targetMs >= 0 && targetMs <= mediaPlayer.getDuration()) {
                            mediaPlayer.seekTo((int) targetMs);
                        }
                    }

                    // Hide overlays after a short delay
                    handler.removeCallbacks(hideGesturesRunnable);
                    handler.postDelayed(hideGesturesRunnable, 1000);
                }
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    // ==================== Gesture Handling ====================

    private void handleBrightnessScroll(float deltaY) {
        if (currentBrightness == -1.0f) {
            Window window = getWindow();
            currentBrightness = window.getAttributes().screenBrightness;
            if (currentBrightness < 0) {
                currentBrightness = 0.5f; // Default if unknown
            }
        }

        // Calculate change based on screen height
        float height = getResources().getDisplayMetrics().heightPixels;
        float change = -(deltaY / height) * 2f; // Multiplier for sensitivity

        float newBrightness = currentBrightness + change;
        newBrightness = Math.max(0.01f, Math.min(1.0f, newBrightness));

        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.screenBrightness = newBrightness;
        window.setAttributes(layoutParams);

        currentBrightness = newBrightness;

        // Update UI
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

        // Update UI
        int percent = (int) (((float) newVolume / maxVolume) * 100);
        volumeOverlay.setVisibility(View.VISIBLE);
        volumeProgress.setProgress(percent);
        tvVolumeValue.setText(percent + "%");
    }

    private void handleSeekScroll(float deltaX) {
        if (seekStartPos == -1) {
            seekStartPos = mediaPlayer.getCurrentPosition();
            handler.removeCallbacks(updateProgressRunnable);
        }

        float width = getResources().getDisplayMetrics().widthPixels;
        long duration = mediaPlayer.getDuration();
        
        // Seek up to 5 minutes based on screen width swipe
        long maxSeekChange = 5 * 60 * 1000; 
        long change = (long) ((deltaX / width) * maxSeekChange);

        long targetPos = seekStartPos + change;
        targetPos = Math.max(0, Math.min(duration, targetPos));

        // Update UI
        seekOverlay.setVisibility(View.VISIBLE);
        tvSeekPosition.setText(VideoUtils.formatDuration(targetPos));
        
        int offsetSeconds = (int) (change / 1000);
        String offsetStr = (offsetSeconds > 0 ? "+" : "") + offsetSeconds + " sec";
        tvSeekOffset.setText(offsetStr);
    }

    private void seekBy(int ms) {
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

    // ==================== Media Player Lifecycle ====================

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

        String path = videoPaths.get(currentIndex);
        String title = videoTitles.get(currentIndex);
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
        
        // Setup UI limits
        int duration = mp.getDuration();
        seekBar.setMax(duration);
        tvTotalTime.setText(VideoUtils.formatDuration(duration));

        // Adjust video size
        adjustAspectRatio(mp.getVideoWidth(), mp.getVideoHeight());

        // Restore playback position if we have an ID
        if (videoIds != null && currentIndex < videoIds.length) {
            long videoId = videoIds[currentIndex];
            VideoDBHelper db = VideoDBHelper.getInstance(this);
            long savedPosition = db.getWatchPosition(videoId);
            if (savedPosition > 0 && savedPosition < duration) {
                mp.seekTo((int) savedPosition);
            }
        }

        mp.start();
        btnPlayPause.setImageResource(R.drawable.ic_pause);
        
        // Start updating progress
        handler.post(updateProgressRunnable);
        
        // Auto-hide controls
        resetHideControlsTimer();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        btnPlayPause.setImageResource(R.drawable.ic_play);
        handler.removeCallbacks(updateProgressRunnable);
        showControls();
        
        // Save completion state (0 means finished)
        saveWatchPosition(0);
        
        // Auto play next if available
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

    // ==================== Surface Lifecycle ====================

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
        // Handle changes if needed
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isSurfaceCreated = false;
        if (mediaPlayer != null) {
            mediaPlayer.setDisplay(null);
        }
    }

    // ==================== Playback Controls ====================

    private void togglePlayPause() {
        if (!isPrepared) return;

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayPause.setImageResource(R.drawable.ic_play);
            handler.removeCallbacks(updateProgressRunnable);
            showControls(); // Keep controls visible when paused
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
            saveWatchPosition(mediaPlayer.getCurrentPosition());
            currentIndex++;
            loadVideo();
        } else {
            Toast.makeText(this, "Last video", Toast.LENGTH_SHORT).show();
        }
    }

    private void playPrevious() {
        if (currentIndex > 0) {
            saveWatchPosition(mediaPlayer.getCurrentPosition());
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

    // ==================== UI State Management ====================

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
        updateProgress(); // Ensure progress is fresh when shown
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
            
            // Hide lock button after a few seconds
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
        
        // Re-adjust surface size for new orientation
        if (isPrepared && mediaPlayer != null) {
            // Need a slight delay to let the layout settle before calculating new bounds
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

    // ==================== Sleep Timer & Info ====================

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
        handler.removeCallbacks(sleepTimerRunnable);
        
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
                    finish(); // Exit player
                } else {
                    updateSleepTimerDisplay();
                    handler.postDelayed(this, 60000); // 1 minute
                }
            }
        };
        
        handler.postDelayed(sleepTimerRunnable, 60000); // Start after 1 min
        Toast.makeText(this, "Sleep timer set for " + minutes + " minutes", Toast.LENGTH_SHORT).show();
    }

    private void updateSleepTimerDisplay() {
        tvSleepTimer.setText(sleepTimerMinutes + "m left");
    }

    private void showVideoInfo() {
        if (!isPrepared) return;

        String path = videoPaths.get(currentIndex);
        int width = mediaPlayer.getVideoWidth();
        int height = mediaPlayer.getVideoHeight();
        String duration = VideoUtils.formatDuration(mediaPlayer.getDuration());

        String info = "Title: " + videoTitles.get(currentIndex) + "\n\n" +
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

    // ==================== Data Persistence ====================

    private void saveWatchPosition(int position) {
        if (videoIds != null && currentIndex < videoIds.length && isPrepared) {
            long videoId = videoIds[currentIndex];
            long duration = mediaPlayer.getDuration();
            
            VideoDBHelper db = VideoDBHelper.getInstance(this);
            db.saveWatchHistory(videoId, position, duration);
        }
    }

    private void resumePlaybackPosition() {
        // Handled in onPrepared since we need duration
    }

    // ==================== Activity Lifecycle ====================

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
            // Restore UI state
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
        
        // If in landscape mode, back button should exit landscape first
        if (isFullscreen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            return;
        }
        
        super.onBackPressed();
    }
}