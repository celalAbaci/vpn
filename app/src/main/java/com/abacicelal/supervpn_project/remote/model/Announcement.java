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
    private String createdAt;

    @SerializedName("sequenceNumber")
    private int sequenceNumber;

    @SerializedName("language")
    private String language;

    @SerializedName("titleTr")
    private String titleTr;

    @SerializedName("messageTr")
    private String messageTr;

    @SerializedName("titleEn")
    private String titleEn;

    @SerializedName("messageEn")
    private String messageEn;

    @SerializedName("titleDe")
    private String titleDe;

    @SerializedName("messageDe")
    private String messageDe;

    @SerializedName("titleFr")
    private String titleFr;

    @SerializedName("messageFr")
    private String messageFr;

    @SerializedName("titleRu")
    private String titleRu;

    @SerializedName("messageRu")
    private String messageRu;

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

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public String getLanguage() {
        return language;
    }

    public String getTitleTr() {
        return titleTr;
    }

    public String getMessageTr() {
        return messageTr;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public String getMessageEn() {
        return messageEn;
    }

    public String getTitleDe() { return titleDe; }
    public String getMessageDe() { return messageDe; }
    public String getTitleFr() { return titleFr; }
    public String getMessageFr() { return messageFr; }
    public String getTitleRu() { return titleRu; }
    public String getMessageRu() { return messageRu; }
}
