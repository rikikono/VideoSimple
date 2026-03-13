package com.example.videoproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class OnlineVideoListActivity extends Activity {

    private final List<OnlineVideoItem> onlineVideos = new ArrayList<>();

    private ListView listOnlineVideos;
    private ImageButton btnBackOnlineVideos;
    private OnlineVideoListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_video_list);

        initViews();
        setupSampleVideos();
        setupListeners();
    }

    private void initViews() {
        listOnlineVideos = findViewById(R.id.listOnlineVideos);
        btnBackOnlineVideos = findViewById(R.id.btnBackOnlineVideos);

        adapter = new OnlineVideoListAdapter(this, onlineVideos);
        listOnlineVideos.setAdapter(adapter);
    }

    private void setupSampleVideos() {
        onlineVideos.clear();
        onlineVideos.add(new OnlineVideoItem(
                "Big Buck Bunny",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                "Public MP4 sample for online playback demo."
        ));
        onlineVideos.add(new OnlineVideoItem(
                "Elephant Dream",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                "Short public demo video over HTTPS."
        ));
        onlineVideos.add(new OnlineVideoItem(
                "For Bigger Blazes",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                "Another direct MP4 sample to verify player stability."
        ));

        adapter.notifyDataSetChanged();
    }

    private void setupListeners() {
        btnBackOnlineVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        listOnlineVideos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openOnlineVideo(position);
            }
        });
    }

    private void openOnlineVideo(int position) {
        if (position < 0 || position >= onlineVideos.size()) {
            return;
        }

        ArrayList<String> urls = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();

        for (OnlineVideoItem item : onlineVideos) {
            urls.add(item.getUrl());
            titles.add(item.getTitle());
        }

        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra("is_online_video", true);
        intent.putStringArrayListExtra("video_paths", urls);
        intent.putStringArrayListExtra("video_titles", titles);
        intent.putExtra("current_index", position);
        startActivity(intent);
    }
}