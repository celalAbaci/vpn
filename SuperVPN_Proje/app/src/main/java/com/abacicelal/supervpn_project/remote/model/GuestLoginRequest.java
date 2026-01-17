package com.abacicelal.supervpn_project.remote.model;

public class GuestLoginRequest {
    private String uniqueDeviceId;
    private String deviceName;

    public GuestLoginRequest(String uniqueDeviceId, String deviceName) {
        this.uniqueDeviceId = uniqueDeviceId;
        this.deviceName = deviceName;
    }
}
