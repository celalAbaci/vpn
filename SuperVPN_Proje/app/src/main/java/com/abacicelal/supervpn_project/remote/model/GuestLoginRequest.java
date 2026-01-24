package com.abacicelal.supervpn_project.remote.model;

public class GuestLoginRequest {
    private String uniqueDeviceId;

    public GuestLoginRequest(String uniqueDeviceId) {
        this.uniqueDeviceId = uniqueDeviceId;
    }

    public String getUniqueDeviceId() {
        return uniqueDeviceId;
    }

    public void setUniqueDeviceId(String uniqueDeviceId) {
        this.uniqueDeviceId = uniqueDeviceId;
    }
}
