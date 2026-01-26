package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

public class GuestLoginRequest {
    @SerializedName("uniqueDeviceId")
    private String uniqueDeviceId;

    public GuestLoginRequest(String uniqueDeviceId) {
        this.uniqueDeviceId = uniqueDeviceId;
    }

    public String getUniqueDeviceId() {
        return uniqueDeviceId;
    }
}
