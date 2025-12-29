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

    public void setToken(String token) {
        this.token = token;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
