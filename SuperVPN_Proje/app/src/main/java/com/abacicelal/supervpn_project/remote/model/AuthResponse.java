package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

/**
 * Login ve Register işlemlerinden dönen yanıt modeli.
 * Backend'deki `com.celalabaci.dto.AuthResponse` ile eşleşir.
 */
public class AuthResponse {

    @SerializedName("accessToken") // Backend JSON: accessToken
    private String accessToken;

    @SerializedName("refreshToken") // Backend JSON: refreshToken
    private String refreshToken;

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    // Gerekirse setter eklenebilir
}
