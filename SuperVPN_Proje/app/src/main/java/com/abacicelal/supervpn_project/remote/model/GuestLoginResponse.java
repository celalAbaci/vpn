package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

public class GuestLoginResponse {
    @SerializedName("accessToken")
    private String accessToken;

    @SerializedName("message")
    private String message;

    public String getAccessToken() {
        return accessToken;
    }

    public String getMessage() {
        return message;
    }
}
