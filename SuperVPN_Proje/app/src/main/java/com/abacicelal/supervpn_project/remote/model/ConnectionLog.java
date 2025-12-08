package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

/**
 * GET /api/v1/logs/my için model.
 * Backend'deki `com.celalabaci.dto.log.UserConnectionLogDto` ile eşleşir.
 */
public class ConnectionLog {

    @SerializedName("id")
    private Long id;

    // 'user' alanı backend'de var
    // private UserDto user;

    // Backend'de DeviceInfoDto var, ancak basitlik için sadece ID alabiliriz
    // veya tam bir DeviceInfoDto sınıfı oluşturabiliriz.
    // Şimdilik null bırakalım, gerekirse eklenir.

    // Backend'de ServerInfoDto var
    // private ServerInfoDto server;

    @SerializedName("connectTime")
    private String connectTime; // Backend 'OffsetDateTime'

    @SerializedName("disconnectTime")
    private String disconnectTime; // Backend 'OffsetDateTime'

    @SerializedName("dataUsedMb")
    private BigDecimal dataUsedMb;

    @SerializedName("createdAt")
    private String createdAt; // Backend 'OffsetDateTime'

    // Getter
    public Long getId() {
        return id;
    }

    public String getConnectTime() {
        return connectTime;
    }

    public String getDisconnectTime() {
        return disconnectTime;
    }

    public BigDecimal getDataUsedMb() {
        return dataUsedMb;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
