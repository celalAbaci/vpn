package com.abacicelal.supervpn_project.remote.model;

public class GuestAuthRequest {
    private String deviceIdentifier;
    private String deviceName;

    public GuestAuthRequest(String deviceIdentifier, String deviceName) {
        this.deviceIdentifier = deviceIdentifier;
        this.deviceName = deviceName;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
