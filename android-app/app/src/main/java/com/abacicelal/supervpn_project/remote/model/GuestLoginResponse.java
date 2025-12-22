package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

public class GuestLoginResponse {
    @SerializedName("token")
    private String token;

    @SerializedName("deviceId")
    private String deviceId;

    public String getToken() {
        return token;
    }

    public String getDeviceId() {
        return deviceId;
    }
}
