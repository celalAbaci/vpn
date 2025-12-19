package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

/**
 * POST /api/v1/auth/refresh-token için model
 * Backend'deki `com.celalabaci.dto.RefreshTokenRequest` ile eşleşir.
 */
public class RefreshTokenRequest {

    @SerializedName("refreshToken")
    private String refreshToken;

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
