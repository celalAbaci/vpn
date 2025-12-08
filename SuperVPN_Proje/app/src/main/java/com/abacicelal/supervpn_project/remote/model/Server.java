package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

/**
 * VPN Sunucusu model sınıfı.
 * Backend'deki `com.celalabaci.dto.vpnserver.VpnServerDto` ile eşleşir.
 * Örnek dosya yerine backend ile tam uyumlu hale getirildi.
 */
public class Server {

    @SerializedName("id")
    private Long id;

    @SerializedName("serverName")
    private String serverName;

    @SerializedName("serverIpAddress")
    private String serverIpAddress;

    @SerializedName("country")
    private Country country; // Backend'den CountryDto nesnesi gelir

    @SerializedName("currentLoadPercentage")
    private Float currentLoadPercentage;

    @SerializedName("currentConnectedUsers")
    private Integer currentConnectedUsers;

    @SerializedName("isActive")
    private boolean isActive;

    // Getter
    public Long getId() {
        return id;
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerIpAddress() {
        return serverIpAddress;
    }

    public Country getCountry() {
        return country;
    }

    public Float getCurrentLoadPercentage() {
        return currentLoadPercentage;
    }

    public Integer getCurrentConnectedUsers() {
        return currentConnectedUsers;
    }

    public boolean isActive() {
        return isActive;
    }
}
