package com.example.videoproject;

public class OnlineVideoItem {
    private final String title;
    private final String url;
    private final String description;

    public OnlineVideoItem(String title, String url, String description) {
        this.title = title;
        this.url = url;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }
}