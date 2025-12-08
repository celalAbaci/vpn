package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

/**
 * GET /api/v1/user/heartbeat için yanıt modeli.
 * Backend'deki `com.celalabaci.dto.user.HeartbeatResponse` ile eşleşir.
 */
public class HeartbeatResponse {

    @SerializedName("active")
    private boolean active;

    @SerializedName("reason")
    private String reason;

    // Getter
    public boolean isActive() {
        return active;
    }

    public String getReason() {
        return reason;
    }
}
