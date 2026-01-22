package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

public class GuestLoginRequest {
    @SerializedName("deviceId")
    private String deviceId;

    public GuestLoginRequest(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
