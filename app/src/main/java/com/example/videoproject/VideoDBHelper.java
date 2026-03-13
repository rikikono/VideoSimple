package com.example.videoproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite database helper for managing watch history, playlists,
 * and playlist-video associations.
 */
public class VideoDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "video_player.db";
    private static final int DATABASE_VERSION = 1;

    // ==================== Watch History Table ====================
    public static final String TABLE_HISTORY = "watch_history";
    public static final String HISTORY_ID = "_id";
    public static final String HISTORY_VIDEO_PATH = "video_path";
    public static final String HISTORY_VIDEO_ID = "video_id";
    public static final String HISTORY_POSITION = "last_position";
    public static final String HISTORY_DURATION = "duration";
    public static final String HISTORY_LAST_WATCHED = "last_watched";

    // ==================== Playlist Table ====================
    public static final String TABLE_PLAYLISTS = "playlists";
    public static final String PLAYLIST_ID = "_id";
    public static final String PLAYLIST_NAME = "name";
    public static final String PLAYLIST_CREATED = "created_date";

    // ==================== Playlist-Video Junction Table ====================
    public static final String TABLE_PLAYLIST_VIDEOS = "playlist_videos";
    public static final String PV_ID = "_id";
    public static final String PV_PLAYLIST_ID = "playlist_id";
    public static final String PV_VIDEO_ID = "video_id";
    public static final String PV_VIDEO_PATH = "video_path";
    public static final String PV_ORDER = "sort_order";

    // SQL to create tables
    private static final String CREATE_HISTORY_TABLE =
            "CREATE TABLE " + TABLE_HISTORY + " (" +
                    HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    HISTORY_VIDEO_PATH + " TEXT, " +
                    HISTORY_VIDEO_ID + " INTEGER UNIQUE, " +
                    HISTORY_POSITION + " INTEGER DEFAULT 0, " +
                    HISTORY_DURATION + " INTEGER DEFAULT 0, " +
                    HISTORY_LAST_WATCHED + " INTEGER DEFAULT 0" +
                    ");";

    private static final String CREATE_PLAYLISTS_TABLE =
            "CREATE TABLE " + TABLE_PLAYLISTS + " (" +
                    PLAYLIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PLAYLIST_NAME + " TEXT NOT NULL, " +
                    PLAYLIST_CREATED + " INTEGER DEFAULT 0" +
                    ");";

    private static final String CREATE_PLAYLIST_VIDEOS_TABLE =
            "CREATE TABLE " + TABLE_PLAYLIST_VIDEOS + " (" +
                    PV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PV_PLAYLIST_ID + " INTEGER NOT NULL, " +
                    PV_VIDEO_ID + " INTEGER DEFAULT 0, " +
                    PV_VIDEO_PATH + " TEXT NOT NULL, " +
                    PV_ORDER + " INTEGER DEFAULT 0, " +
                    "FOREIGN KEY(" + PV_PLAYLIST_ID + ") REFERENCES " +
                    TABLE_PLAYLISTS + "(" + PLAYLIST_ID + ") ON DELETE CASCADE" +
                    ");";

    private static VideoDBHelper sInstance;

    /**
     * Get singleton instance of VideoDBHelper.
     */
    public static synchronized VideoDBHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new VideoDBHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private VideoDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_HISTORY_TABLE);
        db.execSQL(CREATE_PLAYLISTS_TABLE);
        db.execSQL(CREATE_PLAYLIST_VIDEOS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST_VIDEOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLISTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Enable foreign key support
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    // ==================== Watch History Methods ====================

    /**
     * Save or update the playback position for a video (by video path and ID).
     *
     * @param videoPath    File path of the video
     * @param videoId      MediaStore ID
     * @param positionMs   Current playback position in milliseconds
     */
    public void savePlaybackPosition(String videoPath, long videoId, long positionMs) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(HISTORY_VIDEO_PATH, videoPath);
        values.put(HISTORY_VIDEO_ID, videoId);
        values.put(HISTORY_POSITION, positionMs);
        values.put(HISTORY_LAST_WATCHED, System.currentTimeMillis());

        // Use REPLACE to insert or update
        db.insertWithOnConflict(TABLE_HISTORY, null, values,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Save watch history using video ID, position, and duration.
     * Called from VideoPlayerActivity.
     *
     * @param videoId   MediaStore video ID
     * @param position  Current position in milliseconds
     * @param duration  Total duration in milliseconds
     */
    public void saveWatchHistory(long videoId, int position, long duration) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(HISTORY_VIDEO_ID, videoId);
        values.put(HISTORY_POSITION, position);
        values.put(HISTORY_DURATION, duration);
        values.put(HISTORY_LAST_WATCHED, System.currentTimeMillis());

        // Try update first
        int rows = db.update(TABLE_HISTORY, values,
                HISTORY_VIDEO_ID + " = ?",
                new String[]{String.valueOf(videoId)});

        if (rows == 0) {
            // Insert new
            db.insert(TABLE_HISTORY, null, values);
        }
    }

    /**
     * Get the saved playback position for a video by path.
     *
     * @param videoPath File path of the video
     * @return Saved position in milliseconds, or -1 if not found
     */
    public long getPlaybackPosition(String videoPath) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_HISTORY,
                    new String[]{HISTORY_POSITION},
                    HISTORY_VIDEO_PATH + " = ?",
                    new String[]{videoPath},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return -1;
    }

    /**
     * Get the saved watch position for a video by MediaStore ID.
     * Called from VideoPlayerActivity.onPrepared().
     *
     * @param videoId MediaStore video ID
     * @return Saved position in milliseconds, or -1 if not found
     */
    public long getWatchPosition(long videoId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_HISTORY,
                    new String[]{HISTORY_POSITION},
                    HISTORY_VIDEO_ID + " = ?",
                    new String[]{String.valueOf(videoId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return -1;
    }

    /**
     * Clear playback position for a specific video.
     */
    public void clearPlaybackPosition(String videoPath) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_HISTORY,
                HISTORY_VIDEO_PATH + " = ?",
                new String[]{videoPath});
    }

    // ==================== Playlist Methods ====================

    /**
     * Create a new playlist.
     *
     * @param name Playlist name
     * @return ID of the new playlist
     */
    public long createPlaylist(String name) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PLAYLIST_NAME, name);
        values.put(PLAYLIST_CREATED, System.currentTimeMillis());
        return db.insert(TABLE_PLAYLISTS, null, values);
    }

    /**
     * Get all playlists.
     *
     * @return List of playlists
     */
    public List<PlaylistItem> getAllPlaylists() {
        List<PlaylistItem> playlists = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_PLAYLISTS,
                    null, null, null, null, null,
                    PLAYLIST_CREATED + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndexOrThrow(PLAYLIST_ID);
                int nameCol = cursor.getColumnIndexOrThrow(PLAYLIST_NAME);
                int createdCol = cursor.getColumnIndexOrThrow(PLAYLIST_CREATED);

                do {
                    PlaylistItem item = new PlaylistItem();
                    item.id = cursor.getLong(idCol);
                    item.name = cursor.getString(nameCol);
                    item.createdDate = cursor.getLong(createdCol);
                    item.videoCount = getPlaylistVideoCount(item.id);
                    playlists.add(item);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return playlists;
    }

    /**
     * Rename a playlist.
     *
     * @param playlistId Playlist ID
     * @param newName    New name
     * @return true if renamed successfully
     */
    public boolean renamePlaylist(long playlistId, String newName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PLAYLIST_NAME, newName);
        int rows = db.update(TABLE_PLAYLISTS, values,
                PLAYLIST_ID + " = ?",
                new String[]{String.valueOf(playlistId)});
        return rows > 0;
    }

    /**
     * Delete a playlist and all its video associations.
     *
     * @param playlistId Playlist ID
     */
    public void deletePlaylist(long playlistId) {
        SQLiteDatabase db = getWritableDatabase();
        // Delete video associations first
        db.delete(TABLE_PLAYLIST_VIDEOS,
                PV_PLAYLIST_ID + " = ?",
                new String[]{String.valueOf(playlistId)});
        // Delete playlist
        db.delete(TABLE_PLAYLISTS,
                PLAYLIST_ID + " = ?",
                new String[]{String.valueOf(playlistId)});
    }

    /**
     * Get playlist name by ID.
     */
    public String getPlaylistName(long playlistId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_PLAYLISTS,
                    new String[]{PLAYLIST_NAME},
                    PLAYLIST_ID + " = ?",
                    new String[]{String.valueOf(playlistId)},
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return "";
    }

    // ==================== Playlist-Video Methods ====================

    /**
     * Add a video to a playlist.
     *
     * @param playlistId Playlist ID
     * @param videoId    MediaStore video ID
     * @param videoPath  Video file path
     * @return Row ID of the inserted mapping, or -1 on error
     */
    public long addVideoToPlaylist(long playlistId, long videoId, String videoPath) {
        SQLiteDatabase db = getWritableDatabase();

        // Get the current max order
        int maxOrder = 0;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT MAX(" + PV_ORDER + ") FROM " + TABLE_PLAYLIST_VIDEOS +
                            " WHERE " + PV_PLAYLIST_ID + " = ?",
                    new String[]{String.valueOf(playlistId)});
            if (cursor != null && cursor.moveToFirst()) {
                maxOrder = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        ContentValues values = new ContentValues();
        values.put(PV_PLAYLIST_ID, playlistId);
        values.put(PV_VIDEO_ID, videoId);
        values.put(PV_VIDEO_PATH, videoPath);
        values.put(PV_ORDER, maxOrder + 1);
        return db.insert(TABLE_PLAYLIST_VIDEOS, null, values);
    }

    /**
     * Remove a video from a playlist by playlist ID and video path.
     *
     * @param playlistId Playlist ID
     * @param videoPath  Video file path
     */
    public void removeVideoFromPlaylist(long playlistId, String videoPath) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_PLAYLIST_VIDEOS,
                PV_PLAYLIST_ID + " = ? AND " + PV_VIDEO_PATH + " = ?",
                new String[]{String.valueOf(playlistId), videoPath});
    }

    /**
     * Remove a video from a playlist by mapping ID (the _id in playlist_videos table).
     * Used by PlaylistDetailActivity when cleaning up deleted videos.
     *
     * @param mappingId The _id of the playlist_videos row
     */
    public void removeVideoFromPlaylist(long mappingId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_PLAYLIST_VIDEOS,
                PV_ID + " = ?",
                new String[]{String.valueOf(mappingId)});
    }

    /**
     * Get all video paths in a playlist, ordered by sort_order.
     *
     * @param playlistId Playlist ID
     * @return List of video paths
     */
    public List<String> getPlaylistVideoPaths(long playlistId) {
        List<String> paths = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_PLAYLIST_VIDEOS,
                    new String[]{PV_VIDEO_PATH},
                    PV_PLAYLIST_ID + " = ?",
                    new String[]{String.valueOf(playlistId)},
                    null, null, PV_ORDER + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    paths.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return paths;
    }

    /**
     * Get all videos in a playlist as PlaylistVideo objects (with mapping IDs).
     * Used by PlaylistDetailActivity and VideoPickerActivity.
     *
     * @param playlistId Playlist ID
     * @return List of PlaylistVideo objects ordered by sort_order
     */
    public List<PlaylistVideo> getVideosInPlaylist(long playlistId) {
        List<PlaylistVideo> result = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_PLAYLIST_VIDEOS,
                    new String[]{PV_ID, PV_VIDEO_ID, PV_VIDEO_PATH, PV_ORDER},
                    PV_PLAYLIST_ID + " = ?",
                    new String[]{String.valueOf(playlistId)},
                    null, null, PV_ORDER + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndexOrThrow(PV_ID);
                int videoIdCol = cursor.getColumnIndexOrThrow(PV_VIDEO_ID);
                int pathCol = cursor.getColumnIndexOrThrow(PV_VIDEO_PATH);
                int orderCol = cursor.getColumnIndexOrThrow(PV_ORDER);

                do {
                    PlaylistVideo pv = new PlaylistVideo();
                    pv.id = cursor.getLong(idCol);
                    pv.videoId = cursor.getLong(videoIdCol);
                    pv.videoPath = cursor.getString(pathCol);
                    pv.sortOrder = cursor.getInt(orderCol);
                    result.add(pv);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return result;
    }

    /**
     * Get the number of videos in a playlist.
     */
    public int getPlaylistVideoCount(long playlistId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + TABLE_PLAYLIST_VIDEOS +
                            " WHERE " + PV_PLAYLIST_ID + " = ?",
                    new String[]{String.valueOf(playlistId)});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return 0;
    }

    /**
     * Swap the order of two videos in a playlist by their paths.
     *
     * @param playlistId Playlist ID
     * @param path1      First video path
     * @param path2      Second video path
     */
    public void swapVideoOrder(long playlistId, String path1, String path2) {
        SQLiteDatabase db = getWritableDatabase();

        // Get current orders
        int order1 = getVideoOrder(db, playlistId, path1);
        int order2 = getVideoOrder(db, playlistId, path2);

        // Swap
        ContentValues values1 = new ContentValues();
        values1.put(PV_ORDER, order2);
        db.update(TABLE_PLAYLIST_VIDEOS, values1,
                PV_PLAYLIST_ID + " = ? AND " + PV_VIDEO_PATH + " = ?",
                new String[]{String.valueOf(playlistId), path1});

        ContentValues values2 = new ContentValues();
        values2.put(PV_ORDER, order1);
        db.update(TABLE_PLAYLIST_VIDEOS, values2,
                PV_PLAYLIST_ID + " = ? AND " + PV_VIDEO_PATH + " = ?",
                new String[]{String.valueOf(playlistId), path2});
    }

    /**
     * Swap the sort orders of two playlist-video mappings by their mapping IDs.
     * Used by PlaylistDetailActivity for move up/down.
     *
     * @param mappingId1 First mapping ID
     * @param mappingId2 Second mapping ID
     * @return true if swap was successful
     */
    public boolean swapSortOrders(long mappingId1, long mappingId2) {
        SQLiteDatabase db = getWritableDatabase();

        // Get current orders
        int order1 = getSortOrderByMappingId(db, mappingId1);
        int order2 = getSortOrderByMappingId(db, mappingId2);

        if (order1 == -1 || order2 == -1) return false;

        // Swap
        ContentValues values1 = new ContentValues();
        values1.put(PV_ORDER, order2);
        db.update(TABLE_PLAYLIST_VIDEOS, values1,
                PV_ID + " = ?",
                new String[]{String.valueOf(mappingId1)});

        ContentValues values2 = new ContentValues();
        values2.put(PV_ORDER, order1);
        db.update(TABLE_PLAYLIST_VIDEOS, values2,
                PV_ID + " = ?",
                new String[]{String.valueOf(mappingId2)});

        return true;
    }

    private int getVideoOrder(SQLiteDatabase db, long playlistId, String path) {
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_PLAYLIST_VIDEOS,
                    new String[]{PV_ORDER},
                    PV_PLAYLIST_ID + " = ? AND " + PV_VIDEO_PATH + " = ?",
                    new String[]{String.valueOf(playlistId), path},
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return 0;
    }

    private int getSortOrderByMappingId(SQLiteDatabase db, long mappingId) {
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_PLAYLIST_VIDEOS,
                    new String[]{PV_ORDER},
                    PV_ID + " = ?",
                    new String[]{String.valueOf(mappingId)},
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return -1;
    }

    // ==================== Model Classes ====================

    /**
     * Simple model for playlist data.
     */
    public static class PlaylistItem {
        public long id;
        public String name;
        public long createdDate;
        public int videoCount;
    }

    /**
     * Model for a video entry within a playlist (junction table row).
     */
    public static class PlaylistVideo {
        public long id;          // _id in playlist_videos table (mapping ID)
        public long videoId;     // MediaStore video ID
        public String videoPath; // File path
        public int sortOrder;    // Sort order within the playlist
    }
}
