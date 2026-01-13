package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

/**
 * GET /api/v1/announcements için model.
 * Backend'deki `com.celalabaci.dto.announcement.AnnouncementDto` ile eşleşir.
 */
public class Announcement {

    @SerializedName("id")
    private Long id;

    @SerializedName("title")
    private String title;

    @SerializedName("message")
    private String message;

    @SerializedName("isVisible")
    private boolean isVisible;

    @SerializedName("targetUrl")
    private String targetUrl;

    @SerializedName("createdAt")
    private String createdAt; // Backend 'OffsetDateTime'

    // Getter
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
