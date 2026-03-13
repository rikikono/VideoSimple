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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Activity to manage playlists (create, rename, delete) and display a list of them.
 */
public class PlaylistActivity extends Activity {

    private ListView listView;
    private TextView tvEmpty;
    private ImageButton btnBack;
    private ImageButton btnAdd;
    private LinearLayout toolbar;

    private VideoDBHelper dbHelper;
    private PlaylistAdapter adapter;
    private List<VideoDBHelper.PlaylistItem> playlists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        dbHelper = VideoDBHelper.getInstance(this);

        initViews();
        setupListeners();
        updateToolbarColors();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlaylists();
    }

    private void initViews() {
        listView = findViewById(R.id.listPlaylists);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnBack = findViewById(R.id.btnBack);
        btnAdd = findViewById(R.id.btnAddPlaylist);
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

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreatePlaylistDialog();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VideoDBHelper.PlaylistItem playlist = playlists.get(position);
                Intent intent = new Intent(PlaylistActivity.this, PlaylistDetailActivity.class);
                intent.putExtra("playlist_id", playlist.id);
                intent.putExtra("playlist_name", playlist.name);
                startActivity(intent);
            }
        });
    }

    private void loadPlaylists() {
        playlists = dbHelper.getAllPlaylists();
        if (adapter == null) {
            adapter = new PlaylistAdapter(this, playlists);
            listView.setAdapter(adapter);
        } else {
            adapter.updateList(playlists);
        }

        if (playlists.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    private void showCreatePlaylistDialog() {
        final EditText input = new EditText(this);
        input.setHint(R.string.playlist_name);
        
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(this)
                .setTitle(R.string.create_playlist)
                .setView(input)
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = input.getText().toString().trim();
                        if (!name.isEmpty()) {
                            long id = dbHelper.createPlaylist(name);
                            if (id != -1) {
                                loadPlaylists();
                            } else {
                                Toast.makeText(PlaylistActivity.this,
                                        "Playlist already exists or error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showRenameDialog(final VideoDBHelper.PlaylistItem playlist) {
        final EditText input = new EditText(this);
        input.setText(playlist.name);
        input.setSelection(playlist.name.length());
        
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(this)
                .setTitle(R.string.rename)
                .setView(input)
                .setPositiveButton(R.string.rename, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = input.getText().toString().trim();
                        if (!newName.isEmpty() && !newName.equals(playlist.name)) {
                            if (dbHelper.renamePlaylist(playlist.id, newName)) {
                                loadPlaylists();
                            } else {
                                Toast.makeText(PlaylistActivity.this,
                                        "Error renaming playlist", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // ==================== Context Menu ====================

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_playlist_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) return super.onContextItemSelected(item);

        final VideoDBHelper.PlaylistItem playlist = playlists.get(info.position);

        switch (item.getItemId()) {
            case R.id.menu_rename:
                showRenameDialog(playlist);
                return true;
            case R.id.menu_delete:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.delete)
                        .setMessage("Delete playlist '" + playlist.name + "'?")
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dbHelper.deletePlaylist(playlist.id);
                                loadPlaylists();
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