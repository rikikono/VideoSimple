package com.example.musicproject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for selecting multiple videos to add to a playlist.
 */
public class VideoPickerActivity extends Activity {

    private LinearLayout toolbar;
    private ImageButton btnBack;
    private Button btnSave;
    private ListView listView;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private long playlistId;
    private VideoPickerAdapter adapter;
    private List<VideoItem> videoList = new ArrayList<>();
    private VideoDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_picker);

        playlistId = getIntent().getLongExtra("playlist_id", -1);
        if (playlistId == -1) {
            finish();
            return;
        }

        dbHelper = VideoDBHelper.getInstance(this);

        initViews();
        setupListeners();
        updateToolbarColors();
        loadVideos();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnAddSelected);
        listView = findViewById(R.id.listVideos);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        
        adapter = new VideoPickerAdapter(this, videoList);
        listView.setAdapter(adapter);
    }

    private void updateToolbarColors() {
        if (ThemeHelper.isDarkMode(this)) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.toolbarBackground_dark));
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.toolbarBackground));
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSelection();
            }
        });
    }

    private void loadVideos() {
        progressBar.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);

        new AsyncTask<Void, Void, List<VideoItem>>() {
            @Override
            protected List<VideoItem> doInBackground(Void... voids) {
                return VideoUtils.getAllVideos(VideoPickerActivity.this);
            }

            @Override
            protected void onPostExecute(List<VideoItem> result) {
                progressBar.setVisibility(View.GONE);
                videoList.clear();
                videoList.addAll(result);

                if (videoList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    listView.setVisibility(View.VISIBLE);
                    
                    // Mark videos already in playlist
                    List<VideoDBHelper.PlaylistVideo> existing = dbHelper.getVideosInPlaylist(playlistId);
                    List<Long> existingIds = new ArrayList<>();
                    for (VideoDBHelper.PlaylistVideo pv : existing) {
                        existingIds.add(pv.videoId);
                    }
                    adapter.setExistingVideoIds(existingIds);
                }
            }
        }.execute();
    }

    private void saveSelection() {
        List<VideoItem> selected = adapter.getSelectedVideos();
        
        if (selected.isEmpty()) {
            Toast.makeText(this, "No videos selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int addedCount = 0;
        for (VideoItem video : selected) {
            long id = dbHelper.addVideoToPlaylist(playlistId, video.getId(), video.getPath());
            if (id != -1) {
                addedCount++;
            }
        }

        Toast.makeText(this, "Added " + addedCount + " videos", Toast.LENGTH_SHORT).show();
        finish();
    }
}