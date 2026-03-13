package com.example.videoproject;

public class ImageItem {
    private final long id;
    private final String contentUri;

    public ImageItem(long id, String contentUri) {
        this.id = id;
        this.contentUri = contentUri;
    }

    public long getId() {
        return id;
    }

    public String getContentUri() {
        return contentUri;
    }
}