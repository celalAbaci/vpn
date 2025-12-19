package com.celalabaci.dto.vpnserver;

import lombok.Data;

@Data
public class ServerInfoDto {
    private Long id;
    private String serverName;
    private String serverIpAddress;
}
