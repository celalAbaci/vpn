package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

public class GuestLoginRequest {
    @SerializedName("uniqueDeviceId")
    private String uniqueDeviceId;

    @SerializedName("deviceName")
    private String deviceName;

    public GuestLoginRequest(String uniqueDeviceId, String deviceName) {
        this.uniqueDeviceId = uniqueDeviceId;
        this.deviceName = deviceName;
    }
}
