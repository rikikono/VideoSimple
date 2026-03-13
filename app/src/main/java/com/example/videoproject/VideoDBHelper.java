package com.example.videoproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQLite database helper for managing watch history, favorites, playlists,
 * playlist-video associations, bookmarks, and notes.
 */
public class VideoDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "video_player.db";
    private static final int DATABASE_VERSION = 3;

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

    // ==================== Favorites Table ====================
    public static final String TABLE_FAVORITES = "favorites";
    public static final String FAVORITE_ID = "_id";
    public static final String FAVORITE_VIDEO_ID = "video_id";
    public static final String FAVORITE_VIDEO_PATH = "video_path";
    public static final String FAVORITE_CREATED = "created_date";

    // ==================== Bookmarks Table ====================
    public static final String TABLE_BOOKMARKS = "bookmarks";
    public static final String BOOKMARK_ID = "_id";
    public static final String BOOKMARK_VIDEO_ID = "video_id";
    public static final String BOOKMARK_POSITION = "position_ms";
    public static final String BOOKMARK_LABEL = "label";
    public static final String BOOKMARK_CREATED = "created_date";

    // ==================== Notes Table ====================
    public static final String TABLE_NOTES = "notes";
    public static final String NOTE_ID = "_id";
    public static final String NOTE_VIDEO_ID = "video_id";
    public static final String NOTE_POSITION = "position_ms";
    public static final String NOTE_CONTENT = "content";
    public static final String NOTE_CREATED = "created_date";

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

    private static final String CREATE_FAVORITES_TABLE =
            "CREATE TABLE " + TABLE_FAVORITES + " (" +
                    FAVORITE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    FAVORITE_VIDEO_ID + " INTEGER UNIQUE, " +
                    FAVORITE_VIDEO_PATH + " TEXT, " +
                    FAVORITE_CREATED + " INTEGER DEFAULT 0" +
                    ");";

    private static final String CREATE_BOOKMARKS_TABLE =
            "CREATE TABLE " + TABLE_BOOKMARKS + " (" +
                    BOOKMARK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    BOOKMARK_VIDEO_ID + " INTEGER NOT NULL, " +
                    BOOKMARK_POSITION + " INTEGER DEFAULT 0, " +
                    BOOKMARK_LABEL + " TEXT, " +
                    BOOKMARK_CREATED + " INTEGER DEFAULT 0" +
                    ");";

    private static final String CREATE_NOTES_TABLE =
            "CREATE TABLE " + TABLE_NOTES + " (" +
                    NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NOTE_VIDEO_ID + " INTEGER NOT NULL, " +
                    NOTE_POSITION + " INTEGER DEFAULT 0, " +
                    NOTE_CONTENT + " TEXT, " +
                    NOTE_CREATED + " INTEGER DEFAULT 0" +
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
        db.execSQL(CREATE_FAVORITES_TABLE);
        db.execSQL(CREATE_BOOKMARKS_TABLE);
        db.execSQL(CREATE_NOTES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST_VIDEOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLISTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKMARKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    // ==================== Watch History Methods ====================

    public void savePlaybackPosition(String videoPath, long videoId, long positionMs) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(HISTORY_VIDEO_PATH, videoPath);
        values.put(HISTORY_VIDEO_ID, videoId);
        values.put(HISTORY_POSITION, positionMs);
        values.put(HISTORY_LAST_WATCHED, System.currentTimeMillis());

        db.insertWithOnConflict(TABLE_HISTORY, null, values,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void saveWatchHistory(long videoId, int position, long duration) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(HISTORY_VIDEO_ID, videoId);
        values.put(HISTORY_POSITION, position);
        values.put(HISTORY_DURATION, duration);
        values.put(HISTORY_LAST_WATCHED, System.currentTimeMillis());

        int rows = db.update(TABLE_HISTORY, values,
                HISTORY_VIDEO_ID + " = ?",
                new String[]{String.valueOf(videoId)});

        if (rows == 0) {
            db.insert(TABLE_HISTORY, null, values);
        }
    }

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

    public List<WatchHistoryEntry> getRecentWatchHistory(int limit) {
        List<WatchHistoryEntry> result = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            String limitValue = limit > 0 ? String.valueOf(limit) : null;
            cursor = db.query(TABLE_HISTORY,
                    new String[]{HISTORY_VIDEO_ID, HISTORY_POSITION, HISTORY_DURATION, HISTORY_LAST_WATCHED},
                    null, null, null, null,
                    HISTORY_LAST_WATCHED + " DESC",
                    limitValue);

            if (cursor != null && cursor.moveToFirst()) {
                int videoIdCol = cursor.getColumnIndexOrThrow(HISTORY_VIDEO_ID);
                int positionCol = cursor.getColumnIndexOrThrow(HISTORY_POSITION);
                int durationCol = cursor.getColumnIndexOrThrow(HISTORY_DURATION);
                int watchedCol = cursor.getColumnIndexOrThrow(HISTORY_LAST_WATCHED);

                do {
                    WatchHistoryEntry entry = new WatchHistoryEntry();
                    entry.videoId = cursor.getLong(videoIdCol);
                    entry.positionMs = cursor.getLong(positionCol);
                    entry.durationMs = cursor.getLong(durationCol);
                    entry.lastWatched = cursor.getLong(watchedCol);
                    result.add(entry);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return result;
    }

    public Map<Long, Long> getWatchPositionMap() {
        Map<Long, Long> result = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_HISTORY,
                    new String[]{HISTORY_VIDEO_ID, HISTORY_POSITION},
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int videoIdCol = cursor.getColumnIndexOrThrow(HISTORY_VIDEO_ID);
                int positionCol = cursor.getColumnIndexOrThrow(HISTORY_POSITION);
                do {
                    result.put(cursor.getLong(videoIdCol), cursor.getLong(positionCol));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return result;
    }

    public void clearPlaybackPosition(String videoPath) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_HISTORY,
                HISTORY_VIDEO_PATH + " = ?",
                new String[]{videoPath});
    }

    // ==================== Favorites Methods ====================

    public void addFavorite(long videoId, String videoPath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FAVORITE_VIDEO_ID, videoId);
        values.put(FAVORITE_VIDEO_PATH, videoPath);
        values.put(FAVORITE_CREATED, System.currentTimeMillis());
        db.insertWithOnConflict(TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void removeFavorite(long videoId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_FAVORITES,
                FAVORITE_VIDEO_ID + " = ?",
                new String[]{String.valueOf(videoId)});
    }

    public boolean isFavorite(long videoId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_FAVORITES,
                    new String[]{FAVORITE_ID},
                    FAVORITE_VIDEO_ID + " = ?",
                    new String[]{String.valueOf(videoId)},
                    null, null, null);
            return cursor != null && cursor.moveToFirst();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public List<Long> getFavoriteVideoIds() {
        List<Long> result = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_FAVORITES,
                    new String[]{FAVORITE_VIDEO_ID},
                    null, null, null, null,
                    FAVORITE_CREATED + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    result.add(cursor.getLong(0));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return result;
    }

    // ==================== Bookmark Methods ====================

    public long addBookmark(long videoId, long positionMs, String label) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BOOKMARK_VIDEO_ID, videoId);
        values.put(BOOKMARK_POSITION, positionMs);
        values.put(BOOKMARK_LABEL, label);
        values.put(BOOKMARK_CREATED, System.currentTimeMillis());
        return db.insert(TABLE_BOOKMARKS, null, values);
    }

    public void deleteBookmark(long bookmarkId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_BOOKMARKS,
                BOOKMARK_ID + " = ?",
                new String[]{String.valueOf(bookmarkId)});
    }

    public List<BookmarkItem> getBookmarksForVideo(long videoId) {
        List<BookmarkItem> result = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_BOOKMARKS,
                    new String[]{BOOKMARK_ID, BOOKMARK_VIDEO_ID, BOOKMARK_POSITION, BOOKMARK_LABEL, BOOKMARK_CREATED},
                    BOOKMARK_VIDEO_ID + " = ?",
                    new String[]{String.valueOf(videoId)},
                    null, null,
                    BOOKMARK_POSITION + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndexOrThrow(BOOKMARK_ID);
                int videoIdCol = cursor.getColumnIndexOrThrow(BOOKMARK_VIDEO_ID);
                int positionCol = cursor.getColumnIndexOrThrow(BOOKMARK_POSITION);
                int labelCol = cursor.getColumnIndexOrThrow(BOOKMARK_LABEL);
                int createdCol = cursor.getColumnIndexOrThrow(BOOKMARK_CREATED);

                do {
                    BookmarkItem item = new BookmarkItem();
                    item.id = cursor.getLong(idCol);
                    item.videoId = cursor.getLong(videoIdCol);
                    item.positionMs = cursor.getLong(positionCol);
                    item.label = cursor.getString(labelCol);
                    item.createdDate = cursor.getLong(createdCol);
                    result.add(item);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return result;
    }

    // ==================== Notes Methods ====================

    public long addNote(long videoId, long positionMs, String content) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NOTE_VIDEO_ID, videoId);
        values.put(NOTE_POSITION, positionMs);
        values.put(NOTE_CONTENT, content);
        values.put(NOTE_CREATED, System.currentTimeMillis());
        return db.insert(TABLE_NOTES, null, values);
    }

    public void deleteNote(long noteId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NOTES,
                NOTE_ID + " = ?",
                new String[]{String.valueOf(noteId)});
    }

    public List<NoteItem> getNotesForVideo(long videoId) {
        List<NoteItem> result = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NOTES,
                    new String[]{NOTE_ID, NOTE_VIDEO_ID, NOTE_POSITION, NOTE_CONTENT, NOTE_CREATED},
                    NOTE_VIDEO_ID + " = ?",
                    new String[]{String.valueOf(videoId)},
                    null, null,
                    NOTE_POSITION + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndexOrThrow(NOTE_ID);
                int videoIdCol = cursor.getColumnIndexOrThrow(NOTE_VIDEO_ID);
                int positionCol = cursor.getColumnIndexOrThrow(NOTE_POSITION);
                int contentCol = cursor.getColumnIndexOrThrow(NOTE_CONTENT);
                int createdCol = cursor.getColumnIndexOrThrow(NOTE_CREATED);

                do {
                    NoteItem item = new NoteItem();
                    item.id = cursor.getLong(idCol);
                    item.videoId = cursor.getLong(videoIdCol);
                    item.positionMs = cursor.getLong(positionCol);
                    item.content = cursor.getString(contentCol);
                    item.createdDate = cursor.getLong(createdCol);
                    result.add(item);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return result;
    }

    // ==================== Playlist Methods ====================

    public long createPlaylist(String name) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PLAYLIST_NAME, name);
        values.put(PLAYLIST_CREATED, System.currentTimeMillis());
        return db.insert(TABLE_PLAYLISTS, null, values);
    }

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

    public boolean renamePlaylist(long playlistId, String newName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PLAYLIST_NAME, newName);
        int rows = db.update(TABLE_PLAYLISTS, values,
                PLAYLIST_ID + " = ?",
                new String[]{String.valueOf(playlistId)});
        return rows > 0;
    }

    public void deletePlaylist(long playlistId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_PLAYLIST_VIDEOS,
                PV_PLAYLIST_ID + " = ?",
                new String[]{String.valueOf(playlistId)});
        db.delete(TABLE_PLAYLISTS,
                PLAYLIST_ID + " = ?",
                new String[]{String.valueOf(playlistId)});
    }

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

    public long addVideoToPlaylist(long playlistId, long videoId, String videoPath) {
        SQLiteDatabase db = getWritableDatabase();

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

    public void removeVideoFromPlaylist(long playlistId, String videoPath) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_PLAYLIST_VIDEOS,
                PV_PLAYLIST_ID + " = ? AND " + PV_VIDEO_PATH + " = ?",
                new String[]{String.valueOf(playlistId), videoPath});
    }

    public void removeVideoFromPlaylist(long mappingId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_PLAYLIST_VIDEOS,
                PV_ID + " = ?",
                new String[]{String.valueOf(mappingId)});
    }

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

    public void swapVideoOrder(long playlistId, String path1, String path2) {
        SQLiteDatabase db = getWritableDatabase();

        int order1 = getVideoOrder(db, playlistId, path1);
        int order2 = getVideoOrder(db, playlistId, path2);

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

    public boolean swapSortOrders(long mappingId1, long mappingId2) {
        SQLiteDatabase db = getWritableDatabase();

        int order1 = getSortOrderByMappingId(db, mappingId1);
        int order2 = getSortOrderByMappingId(db, mappingId2);

        if (order1 == -1 || order2 == -1) return false;

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

    public static class WatchHistoryEntry {
        public long videoId;
        public long positionMs;
        public long durationMs;
        public long lastWatched;
    }

    public static class BookmarkItem {
        public long id;
        public long videoId;
        public long positionMs;
        public String label;
        public long createdDate;
    }

    public static class NoteItem {
        public long id;
        public long videoId;
        public long positionMs;
        public String content;
        public long createdDate;
    }

    public static class PlaylistItem {
        public long id;
        public String name;
        public long createdDate;
        public int videoCount;
    }

    public static class PlaylistVideo {
        public long id;
        public long videoId;
        public String videoPath;
        public int sortOrder;
    }
}
