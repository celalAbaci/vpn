package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

public class GuestLoginResponse {
    @SerializedName("accessToken")
    private String accessToken;

    @SerializedName("deviceId")
    private Long deviceId;

    public String getAccessToken() {
        return accessToken;
    }

    public Long getDeviceId() {
        return deviceId;
    }
}
