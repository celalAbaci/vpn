package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

/**
 * POST /api/v1/devices/my için istek modeli.
 * Backend'deki `com.celalabaci.dto.userdevice.UserDeviceCreateDto` ile eşleşir.
 */
public class DeviceRequest {

    @SerializedName("deviceName")
    private String deviceName;

    public DeviceRequest(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
