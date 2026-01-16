package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

/**
 * POST /api/v1/config/generate için istek modeli.
 * Backend'deki `com.celalabaci.dto.config.VpnConfigGenerationRequest` ile eşleşir.
 */
public class ConfigGenerationRequest {

    @SerializedName("entryServerId")
    private Long entryServerId;

    @SerializedName("exitServerId")
    private Long exitServerId; // Multi-hop için (opsiyonel)

    @SerializedName("protocol")
    private String protocol; // VpnProtocol enum'ının String hali ("WIREGUARD", "OPENVPN", "IKEV2")

    @SerializedName("deviceId")
    private Long deviceId;

    @SerializedName("dnsProvider")
    private String dnsProvider; // CustomDnsProvider enum'ının String hali ("DEFAULT", "ADGUARD" vb.) (opsiyonel)

    // Constructor
    public ConfigGenerationRequest(Long entryServerId, Long deviceId, VpnProtocol protocol) {
        this.entryServerId = entryServerId;
        this.deviceId = deviceId;
        this.protocol = protocol.name();
        this.dnsProvider = CustomDnsProvider.DEFAULT.name(); // Varsayılan
    }

    // Setters
    public void setEntryServerId(Long entryServerId) {
        this.entryServerId = entryServerId;
    }

    public void setExitServerId(Long exitServerId) {
        this.exitServerId = exitServerId;
    }

    public void setProtocol(VpnProtocol protocol) {
        this.protocol = protocol.name();
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public void setDnsProvider(CustomDnsProvider dnsProvider) {
        if (dnsProvider != null) {
            this.dnsProvider = dnsProvider.name();
        } else {
            this.dnsProvider = null;
        }
    }

    @SerializedName("guestDeviceId")
    private String guestDeviceId;

    public void setGuestDeviceId(String guestDeviceId) {
        this.guestDeviceId = guestDeviceId;
    }
}
