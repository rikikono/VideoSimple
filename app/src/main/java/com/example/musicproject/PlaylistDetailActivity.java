package com.example.musicproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity displaying the videos within a specific playlist.
 * Allows playing, reordering, removing videos, and adding new ones.
 */
public class PlaylistDetailActivity extends Activity {

    private ListView listView;
    private TextView tvEmpty;
    private TextView tvTitle; // maps to tvPlaylistTitle in layout
    private ImageButton btnBack;
    private ImageButton btnPlayAll;
    private ImageButton btnAdd;
    private LinearLayout toolbar;

    private VideoDBHelper dbHelper;
    private PlaylistVideoAdapter adapter;
    private List<VideoItem> videoList;
    private List<Long> mappingIds;

    private long playlistId;
    private String playlistName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        dbHelper = VideoDBHelper.getInstance(this);

        playlistId = getIntent().getLongExtra("playlist_id", -1);
        playlistName = getIntent().getStringExtra("playlist_name");

        if (playlistId == -1) {
            finish();
            return;
        }

        initViews();
        setupListeners();
        updateToolbarColors();

        tvTitle.setText(playlistName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVideos();
    }

    private void initViews() {
        listView = findViewById(R.id.listVideos);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvTitle = findViewById(R.id.tvPlaylistTitle);
        btnBack = findViewById(R.id.btnBack);
        btnPlayAll = findViewById(R.id.btnPlayAll);
        btnAdd = findViewById(R.id.btnAddVideos);
        toolbar = findViewById(R.id.toolbar);

        registerForContextMenu(listView);
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

        btnPlayAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoList != null && !videoList.isEmpty()) {
                    openVideoPlayer(0);
                }
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlaylistDetailActivity.this, VideoPickerActivity.class);
                intent.putExtra("playlist_id", playlistId);
                startActivity(intent);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openVideoPlayer(position);
            }
        });
    }

    private void loadVideos() {
        // Load the relationship objects from DB
        List<VideoDBHelper.PlaylistVideo> mappings = dbHelper.getVideosInPlaylist(playlistId);

        // Scan media store to get full metadata for the mapped IDs
        List<VideoItem> allVideos = VideoUtils.getAllVideos(this);

        videoList = new ArrayList<>();
        mappingIds = new ArrayList<>();

        for (VideoDBHelper.PlaylistVideo mapping : mappings) {
            // Find the video in MediaStore
            VideoItem matchedVideo = null;
            for (VideoItem v : allVideos) {
                if (v.getId() == mapping.videoId) {
                    matchedVideo = v;
                    break;
                }
            }

            // If found in MediaStore (meaning it hasn't been deleted from disk)
            if (matchedVideo != null) {
                videoList.add(matchedVideo);
                mappingIds.add(mapping.id);
            } else {
                // Video was deleted from disk, clean up our DB
                dbHelper.removeVideoFromPlaylist(mapping.id);
            }
        }

        if (adapter == null) {
            // We pass mappingIds to the adapter so it knows which mapping to interact with for move up/down
            adapter = new PlaylistVideoAdapter(this, videoList, mappingIds, new PlaylistVideoAdapter.PlaylistActionCallback() {
                @Override
                public void onMoveUp(int position) {
                    moveVideo(position, position - 1);
                }

                @Override
                public void onMoveDown(int position) {
                    moveVideo(position, position + 1);
                }
            });
            listView.setAdapter(adapter);
        } else {
            adapter.updateList(videoList, mappingIds);
        }

        if (videoList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    private void moveVideo(int fromPosition, int toPosition) {
        if (toPosition < 0 || toPosition >= videoList.size()) return;

        long mappingId1 = mappingIds.get(fromPosition);
        long mappingId2 = mappingIds.get(toPosition);

        if (dbHelper.swapSortOrders(mappingId1, mappingId2)) {
            // Refresh list
            loadVideos();
        }
    }

    private void openVideoPlayer(int position) {
        Intent intent = new Intent(this, VideoPlayerActivity.class);

        ArrayList<String> paths = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();
        long[] ids = new long[videoList.size()];

        for (int i = 0; i < videoList.size(); i++) {
            VideoItem v = videoList.get(i);
            paths.add(v.getPath());
            titles.add(v.getTitle());
            ids[i] = v.getId();
        }

        intent.putStringArrayListExtra("video_paths", paths);
        intent.putStringArrayListExtra("video_titles", titles);
        intent.putExtra("video_ids", ids);
        intent.putExtra("current_index", position);
        startActivity(intent);
    }

    // ==================== Context Menu ====================

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_playlist_video_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) return super.onContextItemSelected(item);

        final long mappingId = mappingIds.get(info.position);
        final VideoItem video = videoList.get(info.position);

        int action = -1;
        if (item.getItemId() == R.id.menu_remove) {
            action = 0;
        }
        switch (action) {
            case 0:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.remove)
                        .setMessage("Remove '" + video.getTitle() + "' from playlist?")
                        .setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dbHelper.removeVideoFromPlaylist(mappingId);
                                loadVideos();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}