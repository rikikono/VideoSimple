package com.example.musicproject;

/**
 * Model class representing a video file on the device.
 * Contains metadata retrieved from MediaStore.
 */
public class VideoItem {

    private long id;           // MediaStore ID
    private String title;      // Video display name
    private String path;       // Full file path
    private long duration;     // Duration in milliseconds
    private long size;         // File size in bytes
    private long dateAdded;    // Date added timestamp (seconds)
    private long dateModified; // Date modified timestamp (seconds)
    private String mimeType;   // MIME type (e.g., video/mp4)
    private int width;         // Video width in pixels
    private int height;        // Video height in pixels

    public VideoItem() {
    }

    public VideoItem(long id, String title, String path, long duration,
                     long size, long dateAdded, long dateModified,
                     String mimeType, int width, int height) {
        this.id = id;
        this.title = title;
        this.path = path;
        this.duration = duration;
        this.size = size;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.mimeType = mimeType;
        this.width = width;
        this.height = height;
    }

    // ==================== Getters ====================

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    public long getDuration() {
        return duration;
    }

    public long getSize() {
        return size;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public long getDateModified() {
        return dateModified;
    }

    public String getMimeType() {
        return mimeType;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // ==================== Setters ====================

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Format duration from milliseconds to HH:MM:SS or MM:SS string.
     */
    public String getFormattedDuration() {
        return VideoUtils.formatDuration(duration);
    }

    /**
     * Format file size to human-readable string (MB or GB).
     */
    public String getFormattedSize() {
        return VideoUtils.formatFileSize(size);
    }

    /**
     * Get resolution string (e.g., "1920x1080").
     */
    public String getResolution() {
        if (width > 0 && height > 0) {
            return width + "x" + height;
        }
        return "Unknown";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoItem videoItem = (VideoItem) o;
        return id == videoItem.id;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(id).hashCode();
    }
}
