package com.celalabaci.dto.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VpnConfigResponse {
    // Android bu değişken adını bekliyor (String)
    private String configurationFileContent;

    private String protocol;
    private String serverName;
}