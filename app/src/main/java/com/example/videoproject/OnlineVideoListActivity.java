package com.example.videoproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OnlineVideoListActivity extends Activity {

    private final List<OnlineVideoItem> onlineVideos = new ArrayList<>();

    private ListView listOnlineVideos;
    private ImageButton btnBackOnlineVideos;
    private TextView tvOnlineVideosEmpty;
    private TextView tvOnlineVideosHint;
    private android.widget.Button btnAddOnlineUrl;
    private OnlineVideoListAdapter adapter;
    private VideoDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_video_list);

        dbHelper = VideoDBHelper.getInstance(this);

        initViews();
        loadOnlineVideos();
        setupListeners();
    }

    private void initViews() {
        listOnlineVideos = findViewById(R.id.listOnlineVideos);
        btnBackOnlineVideos = findViewById(R.id.btnBackOnlineVideos);
        tvOnlineVideosEmpty = findViewById(R.id.tvOnlineVideosEmpty);
        tvOnlineVideosHint = findViewById(R.id.tvOnlineVideosHint);
        btnAddOnlineUrl = findViewById(R.id.btnAddOnlineUrl);

        adapter = new OnlineVideoListAdapter(this, onlineVideos);
        listOnlineVideos.setAdapter(adapter);
    }

    private void loadOnlineVideos() {
        onlineVideos.clear();
        onlineVideos.addAll(getSampleVideos());
        onlineVideos.addAll(dbHelper.getAllOnlineVideos());
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private List<OnlineVideoItem> getSampleVideos() {
        List<OnlineVideoItem> samples = new ArrayList<>();
        samples.add(new OnlineVideoItem(
                -1,
                "Big Buck Bunny",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                "Public MP4 sample for online playback demo.",
                false,
                0
        ));
        samples.add(new OnlineVideoItem(
                -2,
                "Elephant Dream",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                "Short public demo video over HTTPS.",
                false,
                0
        ));
        samples.add(new OnlineVideoItem(
                -3,
                "For Bigger Blazes",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                "Another direct MP4 sample to verify player stability.",
                false,
                0
        ));
        return samples;
    }

    private void updateEmptyState() {
        if (onlineVideos.isEmpty()) {
            listOnlineVideos.setVisibility(View.GONE);
            tvOnlineVideosEmpty.setVisibility(View.VISIBLE);
        } else {
            listOnlineVideos.setVisibility(View.VISIBLE);
            tvOnlineVideosEmpty.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        btnBackOnlineVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnAddOnlineUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddOrEditDialog(null);
            }
        });

        listOnlineVideos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openOnlineVideo(position);
            }
        });

        listOnlineVideos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= onlineVideos.size()) {
                    return true;
                }

                OnlineVideoItem item = onlineVideos.get(position);
                if (!item.isUserAdded()) {
                    return true;
                }

                showItemActions(item);
                return true;
            }
        });
    }

    private void showItemActions(final OnlineVideoItem item) {
        final String[] actions = {
                getString(R.string.edit),
                getString(R.string.delete)
        };

        new AlertDialog.Builder(this)
                .setTitle(item.getTitle())
                .setItems(actions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            showAddOrEditDialog(item);
                        } else {
                            confirmDelete(item);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDelete(final OnlineVideoItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage(item.getTitle())
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteOnlineVideo(item.getId());
                        loadOnlineVideos();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showAddOrEditDialog(final OnlineVideoItem editingItem) {
        boolean isEditing = editingItem != null;

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, 0);

        final EditText inputUrl = new EditText(this);
        inputUrl.setHint(R.string.video_url);
        inputUrl.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);

        final EditText inputTitle = new EditText(this);
        inputTitle.setHint(R.string.video_info_title);

        final EditText inputDescription = new EditText(this);
        inputDescription.setHint(R.string.note_hint);
        inputDescription.setMinLines(2);
        inputDescription.setMaxLines(4);

        if (isEditing) {
            inputUrl.setText(editingItem.getUrl());
            inputTitle.setText(editingItem.getTitle());
            inputDescription.setText(editingItem.getDescription());
        }

        container.addView(inputUrl);
        container.addView(inputTitle);
        container.addView(inputDescription);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(isEditing ? R.string.edit_url : R.string.add_url)
                .setView(container)
                .setPositiveButton(isEditing ? R.string.save : R.string.add_url, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String url = safeTrim(inputUrl.getText().toString());
                    String title = safeTrim(inputTitle.getText().toString());
                    String description = safeTrim(inputDescription.getText().toString());

                    String validationError = validateUrl(url, editingItem);
                    if (validationError != null) {
                        Toast.makeText(this, validationError, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (title.isEmpty()) {
                        title = deriveTitleFromUrl(url);
                    }

                    if (isEditing) {
                        boolean updated = dbHelper.updateOnlineVideo(
                                editingItem.getId(),
                                title,
                                url,
                                description
                        );
                        if (!updated) {
                            Toast.makeText(this, R.string.error_playing_video, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        long insertedId = dbHelper.addOnlineVideo(title, url, description);
                        if (insertedId <= 0) {
                            Toast.makeText(this, R.string.url_already_added, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    loadOnlineVideos();
                    dialog.dismiss();
                }));

        dialog.show();
    }

    private String validateUrl(String rawUrl, OnlineVideoItem editingItem) {
        if (rawUrl.isEmpty()) {
            return getString(R.string.url_required);
        }

        Uri uri = Uri.parse(rawUrl);
        String scheme = uri.getScheme();
        String host = uri.getHost();
        String lowerUrl = rawUrl.toLowerCase(Locale.US);

        if (scheme == null || !scheme.equals("https")) {
            return getString(R.string.only_https_direct_links_supported);
        }

        if (host == null || host.trim().isEmpty()) {
            return getString(R.string.unsupported_direct_video_link);
        }

        if (lowerUrl.contains("youtube.com")
                || lowerUrl.contains("youtu.be")
                || lowerUrl.contains("facebook.com")
                || lowerUrl.contains("tiktok.com")) {
            return getString(R.string.unsupported_direct_video_link);
        }

        if (!(lowerUrl.endsWith(".mp4")
                || lowerUrl.contains(".mp4?")
                || lowerUrl.contains(".mp4&"))) {
            return getString(R.string.only_https_direct_links_supported);
        }

        if (editingItem != null) {
            if (dbHelper.hasOnlineVideoUrl(rawUrl, editingItem.getId())) {
                return getString(R.string.url_already_added);
            }
        } else if (dbHelper.hasOnlineVideoUrl(rawUrl) || isSampleUrl(rawUrl)) {
            return getString(R.string.url_already_added);
        }

        return null;
    }

    private boolean isSampleUrl(String url) {
        for (OnlineVideoItem item : getSampleVideos()) {
            if (item.getUrl().equalsIgnoreCase(url)) {
                return true;
            }
        }
        return false;
    }

    private String deriveTitleFromUrl(String url) {
        Uri uri = Uri.parse(url);
        String lastSegment = uri.getLastPathSegment();
        if (lastSegment == null || lastSegment.trim().isEmpty()) {
            return getString(R.string.custom_online_video);
        }

        String clean = lastSegment;
        int queryIndex = clean.indexOf('?');
        if (queryIndex >= 0) {
            clean = clean.substring(0, queryIndex);
        }
        if (clean.trim().isEmpty()) {
            return getString(R.string.custom_online_video);
        }
        return clean;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
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