package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

/**
 * POST /api/v1/config/generate için yanıt modeli.
 * (ApiService'de VpnConfig olarak adlandırılmıştı, VpnConfigResponse olarak güncellendi)
 * Backend'deki `com.celalabaci.dto.config.VpnConfigResponse` ile eşleşir.
 */
public class VpnConfigResponse {

    @SerializedName("configurationFileContent")
    private String configurationFileContent;

    @SerializedName("protocol")
    private String protocol;

    @SerializedName("serverName")
    private String serverName;

    // Getter
    public String getConfigurationFileContent() {
        return configurationFileContent;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getServerName() {
        return serverName;
    }
}
