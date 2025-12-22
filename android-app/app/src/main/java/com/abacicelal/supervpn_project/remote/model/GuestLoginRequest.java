package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

public class GuestLoginRequest {
    @SerializedName("deviceId")
    private String deviceId;

    public GuestLoginRequest(String deviceId) {
        this.deviceId = deviceId;
    }
}
