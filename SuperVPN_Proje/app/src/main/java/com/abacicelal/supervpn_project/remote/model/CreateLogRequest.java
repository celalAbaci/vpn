package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

/**
 * POST /api/v1/logs/my için istek modeli.
 * Backend'deki `com.celalabaci.dto.log.UserConnectionLogCreateDto` ile eşleşir.
 */
public class CreateLogRequest {

    @SerializedName("deviceId")
    private Long deviceId;

    @SerializedName("serverId")
    private Long serverId;

    @SerializedName("connectTime")
    private String connectTime; // "YYYY-MM-DDTHH:mm:ss.SSSZ" formatında gönderilmeli

    @SerializedName("disconnectTime")
    private String disconnectTime; // "YYYY-MM-DDTHH:mm:ss.SSSZ" formatında gönderilmeli (opsiyonel)

    @SerializedName("dataUsedMb")
    private BigDecimal dataUsedMb; // (opsiyonel)

    // Constructor
    public CreateLogRequest(Long deviceId, Long serverId, String connectTime) {
        this.deviceId = deviceId;
        this.serverId = serverId;
        this.connectTime = connectTime;
    }

    // Setters
    public void setDisconnectTime(String disconnectTime) {
        this.disconnectTime = disconnectTime;
    }

    public void setDataUsedMb(BigDecimal dataUsedMb) {
        this.dataUsedMb = dataUsedMb;
    }
}
