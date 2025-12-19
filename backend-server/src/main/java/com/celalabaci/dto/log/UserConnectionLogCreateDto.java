package com.celalabaci.dto.log;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class UserConnectionLogCreateDto {
    @NotNull(message = "Device ID cannot be null")
    private Long deviceId;

    @NotNull(message = "Server ID cannot be null")
    private Long serverId;

    @NotNull(message = "Connect time cannot be null")
    private OffsetDateTime connectTime;

    private OffsetDateTime disconnectTime;

    @Positive(message = "Data used must be positive")
    private BigDecimal dataUsedMb;
}
