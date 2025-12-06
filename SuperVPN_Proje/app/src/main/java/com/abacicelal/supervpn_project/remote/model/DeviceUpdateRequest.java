package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

/**
 * PUT /api/v1/devices/my/{id} için istek modeli.
 * Backend'deki `com.celalabaci.dto.userdevice.UserDeviceUpdateDto` ile eşleşir.
 */
public class DeviceUpdateRequest {

    @SerializedName("deviceName")
    private String deviceName;

    public DeviceUpdateRequest(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
