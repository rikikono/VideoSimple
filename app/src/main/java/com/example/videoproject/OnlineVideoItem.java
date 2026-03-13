package com.example.videoproject;

public class OnlineVideoItem {
    private final long id;
    private final String title;
    private final String url;
    private final String description;
    private final boolean userAdded;
    private final long createdAt;

    public OnlineVideoItem(long id, String title, String url, String description,
                           boolean userAdded, long createdAt) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.description = description;
        this.userAdded = userAdded;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
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

    public boolean isUserAdded() {
        return userAdded;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getSourceLabel() {
        return userAdded ? "Custom URL" : "Sample";
    }
}