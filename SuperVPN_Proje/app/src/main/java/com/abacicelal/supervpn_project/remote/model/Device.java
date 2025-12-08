package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

/**
 * Cihaz model sınıfı.
 * Backend'deki `com.celalabaci.dto.userdevice.UserDeviceDto` ile eşleşir.
 */
public class Device {

    @SerializedName("id")
    private Long id;

    // Backend'de UserDto var, ancak Android tarafında genellikle
    // tüm kullanıcı bilgisine ihtiyaç duyulmaz.
    // İhtiyaç duyulursa 'private UserDto user;' eklenebilir.

    @SerializedName("deviceName")
    private String deviceName;

    @SerializedName("lastSeen")
    private String lastSeen; // Backend 'OffsetDateTime' gönderir, String olarak almak en güvenlisidir.

    @SerializedName("active")
    private boolean active;

    // Getter
    public Long getId() {
        return id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public boolean isActive() {
        return active;
    }
}
