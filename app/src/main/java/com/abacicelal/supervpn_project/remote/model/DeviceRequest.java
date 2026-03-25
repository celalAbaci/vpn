package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

/**
 * POST /api/v1/devices/my için istek modeli.
 * Backend'deki `com.celalabaci.dto.userdevice.UserDeviceCreateDto` ile eşleşir.
 */
public class DeviceRequest {

    @SerializedName("deviceName")
    private String deviceName;

    @SerializedName("deviceIdentifier")
    private String deviceIdentifier;

    @SerializedName("osVersion")
    private String osVersion;

    @SerializedName("appVersion")
    private String appVersion;

    public DeviceRequest(String deviceName, String deviceIdentifier, String osVersion, String appVersion) {
        this.deviceName = deviceName;
        this.deviceIdentifier = deviceIdentifier;
        this.osVersion = osVersion;
        this.appVersion = appVersion;
    }

    public String getDeviceName() { return deviceName; }
    public String getDeviceIdentifier() { return deviceIdentifier; }
    public String getOsVersion() { return osVersion; }
    public String getAppVersion() { return appVersion; }
}
