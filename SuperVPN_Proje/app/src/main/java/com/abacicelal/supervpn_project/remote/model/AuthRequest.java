package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

/**
 * POST /api/v1/auth/authenticate için model
 * Backend'deki `com.celalabaci.dto.AuthRequest` ile eşleşir.
 * UYARI: Backend 'email' değil 'username' bekliyor. Bu dosya düzeltildi.
 */
public class AuthRequest {
    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
