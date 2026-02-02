package com.abacicelal.supervpn_project.remote.model;

public class GuestLoginRequest {
    private String uniqueDeviceId;
    private String deviceName;
    private String deviceModel;

    public GuestLoginRequest(String uniqueDeviceId, String deviceName, String deviceModel) {
        this.uniqueDeviceId = uniqueDeviceId;
        this.deviceName = deviceName;
        this.deviceModel = deviceModel;
    }
}
