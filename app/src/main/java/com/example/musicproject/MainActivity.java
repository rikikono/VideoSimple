package com.example.musicproject;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Main Activity - Video browser that scans and displays all videos on the device.
 * Features: MediaStore scanning, sorting, permission handling, context menu,
 * dark mode toggle, navigation to player and playlists.
 */
public class MainActivity extends Activity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    // UI components
    private LinearLayout rootLayout;
    private LinearLayout toolbar;
    private ListView listVideos;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ImageButton btnSort;
    private ImageButton btnPlaylists;
    private ImageButton btnMore;

    // Data
    private List<VideoItem> videoList = new ArrayList<>();
    private VideoListAdapter adapter;

    // Sorting state
    private int currentSortType = 0; // 0=date, 1=name, 2=size
    private boolean sortAscending = false; // false = descending

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setContentView
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
        updateToolbarColors();

        // Check permissions and load videos
        if (hasStoragePermission()) {
            loadVideos();
        } else {
            requestStoragePermission();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when returning from other activities
        if (hasStoragePermission() && adapter != null) {
            loadVideos();
        }
    }

    /**
     * Initialize all view references.
     */
    private void initViews() {
        rootLayout = findViewById(R.id.rootLayout);
        toolbar = findViewById(R.id.toolbar);
        listVideos = findViewById(R.id.listVideos);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnSort = findViewById(R.id.btnSort);
        btnPlaylists = findViewById(R.id.btnPlaylists);
        btnMore = findViewById(R.id.btnMore);

        // Initialize adapter
        adapter = new VideoListAdapter(this, videoList);
        listVideos.setAdapter(adapter);

        // Register context menu for long press
        registerForContextMenu(listVideos);
    }

    /**
     * Update toolbar colors based on current theme.
     */
    private void updateToolbarColors() {
        if (ThemeHelper.isDarkMode(this)) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.toolbarBackground_dark));
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.toolbarBackground));
        }
    }

    /**
     * Setup click listeners for all interactive elements.
     */
    private void setupListeners() {
        // Video item click - open player
        listVideos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openVideoPlayer(position);
            }
        });

        // Sort button
        btnSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortMenu(v);
            }
        });

        // Playlists button
        btnPlaylists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlaylistActivity.class);
                startActivity(intent);
            }
        });

        // More button
        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreMenu(v);
            }
        });
    }

    // ==================== Permission Handling ====================

    /**
     * Check if the app has the required storage permission.
     */
    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+: READ_MEDIA_VIDEO
            return checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO)
                    == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6-12: READ_EXTERNAL_STORAGE
            return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
        // Pre-Marshmallow: permission granted at install time
        return true;
    }

    /**
     * Request the appropriate storage permission.
     */
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String permission;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permission = Manifest.permission.READ_MEDIA_VIDEO;
            } else {
                permission = Manifest.permission.READ_EXTERNAL_STORAGE;
            }

            // Check if we should show rationale
            if (shouldShowRequestPermissionRationale(permission)) {
                showPermissionRationale(permission);
            } else {
                requestPermissions(new String[]{permission}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    /**
     * Show a rationale dialog explaining why permission is needed.
     */
    private void showPermissionRationale(final String permission) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.permission_title)
                .setMessage(R.string.permission_message)
                .setPositiveButton(R.string.grant, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{permission}, PERMISSION_REQUEST_CODE);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showEmptyState();
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadVideos();
            } else {
                // Permission denied
                new AlertDialog.Builder(this)
                        .setTitle(R.string.permission_denied_title)
                        .setMessage(R.string.permission_denied_message)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showEmptyState();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        }
    }

    // ==================== Video Loading ====================

    /**
     * Load videos from MediaStore asynchronously.
     */
    private void loadVideos() {
        progressBar.setVisibility(View.VISIBLE);
        listVideos.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        new AsyncTask<Void, Void, List<VideoItem>>() {
            @Override
            protected List<VideoItem> doInBackground(Void... voids) {
                return VideoUtils.getAllVideos(MainActivity.this);
            }

            @Override
            protected void onPostExecute(List<VideoItem> result) {
                progressBar.setVisibility(View.GONE);
                videoList.clear();
                videoList.addAll(result);

                if (videoList.isEmpty()) {
                    showEmptyState();
                } else {
                    sortVideos();
                    listVideos.setVisibility(View.VISIBLE);
                    tvEmpty.setVisibility(View.GONE);
                }
            }
        }.execute();
    }

    /**
     * Show empty state when no videos found.
     */
    private void showEmptyState() {
        listVideos.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    // ==================== Sorting ====================

    /**
     * Show sort popup menu.
     */
    private void showSortMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, getString(R.string.sort_by_name));
        popup.getMenu().add(0, 2, 1, getString(R.string.sort_by_date));
        popup.getMenu().add(0, 3, 2, getString(R.string.sort_by_size));
        popup.getMenu().add(0, 4, 3, sortAscending ?
                getString(R.string.sort_descending) : getString(R.string.sort_ascending));

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 1:
                        currentSortType = 1;
                        break;
                    case 2:
                        currentSortType = 0;
                        break;
                    case 3:
                        currentSortType = 2;
                        break;
                    case 4:
                        sortAscending = !sortAscending;
                        break;
                }
                sortVideos();
                return true;
            }
        });
        popup.show();
    }

    /**
     * Sort the video list based on current sort settings.
     */
    private void sortVideos() {
        Comparator<VideoItem> comparator;

        switch (currentSortType) {
            case 1: // Name
                comparator = new Comparator<VideoItem>() {
                    @Override
                    public int compare(VideoItem a, VideoItem b) {
                        String nameA = a.getTitle() != null ? a.getTitle() : "";
                        String nameB = b.getTitle() != null ? b.getTitle() : "";
                        return nameA.compareToIgnoreCase(nameB);
                    }
                };
                break;
            case 2: // Size
                comparator = new Comparator<VideoItem>() {
                    @Override
                    public int compare(VideoItem a, VideoItem b) {
                        return Long.compare(a.getSize(), b.getSize());
                    }
                };
                break;
            default: // Date (0)
                comparator = new Comparator<VideoItem>() {
                    @Override
                    public int compare(VideoItem a, VideoItem b) {
                        return Long.compare(a.getDateAdded(), b.getDateAdded());
                    }
                };
                break;
        }

        Collections.sort(videoList, comparator);

        if (!sortAscending) {
            Collections.reverse(videoList);
        }

        adapter.updateList(videoList);
    }

    // ==================== More Menu ====================

    /**
     * Show more options popup menu (dark mode, refresh).
     */
    private void showMoreMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, ThemeHelper.isDarkMode(this) ?
                getString(R.string.dark_mode_off) : getString(R.string.dark_mode_on));
        popup.getMenu().add(0, 2, 1, getString(R.string.refresh));

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 1:
                        toggleDarkMode();
                        return true;
                    case 2:
                        if (hasStoragePermission()) {
                            adapter.clearCache();
                            loadVideos();
                        }
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }

    /**
     * Toggle dark mode and recreate the activity.
     */
    private void toggleDarkMode() {
        boolean newState = ThemeHelper.toggleDarkMode(this);
        Toast.makeText(this,
                newState ? R.string.dark_mode_on : R.string.dark_mode_off,
                Toast.LENGTH_SHORT).show();
        // Recreate activity to apply new theme
        recreate();
    }

    // ==================== Context Menu ====================

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                     ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_video_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        if (info == null) return super.onContextItemSelected(item);

        VideoItem video = videoList.get(info.position);

        int itemId = item.getItemId();
        if (itemId == R.id.menu_video_info) {
            showVideoInfoDialog(video);
            return true;
        } else if (itemId == R.id.menu_add_to_playlist) {
            showAddToPlaylistDialog(video);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    // ==================== Video Info Dialog ====================

    /**
     * Show dialog with detailed video information.
     */
    private void showVideoInfoDialog(VideoItem video) {
        StringBuilder info = new StringBuilder();
        info.append(getString(R.string.info_resolution)).append(": ")
                .append(video.getResolution()).append("\n\n");
        info.append(getString(R.string.info_size)).append(": ")
                .append(video.getFormattedSize()).append("\n\n");
        info.append(getString(R.string.info_duration)).append(": ")
                .append(video.getFormattedDuration()).append("\n\n");
        info.append(getString(R.string.info_path)).append(": ")
                .append(video.getPath()).append("\n\n");
        info.append(getString(R.string.info_date_added)).append(": ")
                .append(VideoUtils.formatDate(video.getDateAdded())).append("\n\n");
        info.append(getString(R.string.info_mime_type)).append(": ")
                .append(video.getMimeType());

        new AlertDialog.Builder(this)
                .setTitle(R.string.video_info_title)
                .setMessage(info.toString())
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    /**
     * Show dialog to choose a playlist to add the video to.
     */
    private void showAddToPlaylistDialog(final VideoItem video) {
        final VideoDBHelper dbHelper = VideoDBHelper.getInstance(this);
        final List<VideoDBHelper.PlaylistItem> playlists = dbHelper.getAllPlaylists();

        if (playlists.isEmpty()) {
            Toast.makeText(this, R.string.no_playlists, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[playlists.size()];
        for (int i = 0; i < playlists.size(); i++) {
            names[i] = playlists.get(i).name;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.playlists)
                .setItems(names, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        VideoDBHelper.PlaylistItem playlist = playlists.get(which);
                        dbHelper.addVideoToPlaylist(playlist.id, video.getId(), video.getPath());
                        Toast.makeText(MainActivity.this,
                                "Added to " + playlist.name, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // ==================== Navigation ====================

    /**
     * Open the video player with the selected video.
     * Passes the entire video list for next/previous navigation.
     */
    private void openVideoPlayer(int position) {
        Intent intent = new Intent(this, VideoPlayerActivity.class);

        // Pass video paths as an array for next/previous support
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
}
