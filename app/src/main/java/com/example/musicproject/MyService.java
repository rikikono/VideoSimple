package com.example.musicproject;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {
    MediaPlayer mediaPlayer;
    private int[] songList = {R.raw.trentinhbanduoitinhyeu, R.raw.tetonroi, R.raw.nhungngaybaola};
    private String[] songNames = {"Trên Tình Bạn Dưới Tình Yêu", "Tết Tôn Rồi", "Những Ngày Bão La"};
    private int currentSongIndex = 0;
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_UPDATE_SONG = "com.example.musicproject.UPDATE_SONG";
    public static final String EXTRA_SONG_NAME = "EXTRA_SONG_NAME";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(MyService.this, songList[currentSongIndex]);
        mediaPlayer.setLooping(false);
        mediaPlayer.setOnCompletionListener(mp -> {
            playNext();
        });
        sendSongUpdate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getStringExtra("action");
        
        if (action != null) {
            switch (action) {
                case ACTION_PLAY:
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    } else {
                        mediaPlayer.start();
                    }
                    sendSongUpdate();
                    break;
                case ACTION_NEXT:
                    playNext();
                    break;
                case ACTION_PREVIOUS:
                    playPrevious();
                    break;
            }
        } else {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void playNext() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        currentSongIndex = (currentSongIndex + 1) % songList.length;
        mediaPlayer = MediaPlayer.create(MyService.this, songList[currentSongIndex]);
        mediaPlayer.setOnCompletionListener(mp -> {
            playNext();
        });
        mediaPlayer.start();
        sendSongUpdate();
    }

    private void playPrevious() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        currentSongIndex = (currentSongIndex - 1 + songList.length) % songList.length;
        mediaPlayer = MediaPlayer.create(MyService.this, songList[currentSongIndex]);
        mediaPlayer.setOnCompletionListener(mp -> {
            playNext();
        });
        mediaPlayer.start();
        sendSongUpdate();
    }

    private void sendSongUpdate() {
        Intent intent = new Intent(ACTION_UPDATE_SONG);
        intent.putExtra(EXTRA_SONG_NAME, songNames[currentSongIndex]);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
