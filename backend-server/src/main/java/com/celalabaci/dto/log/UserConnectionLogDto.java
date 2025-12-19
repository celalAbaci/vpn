package com.celalabaci.dto.log;

import com.celalabaci.dto.user.UserDto;
import com.celalabaci.dto.userdevice.DeviceInfoDto;
import com.celalabaci.dto.vpnserver.ServerInfoDto;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class UserConnectionLogDto {
    private Long id;
    private UserDto user;
    private DeviceInfoDto device;
    private ServerInfoDto server;
    private OffsetDateTime connectTime;
    private OffsetDateTime disconnectTime;
    private BigDecimal dataUsedMb;
    private OffsetDateTime createdAt;
}
