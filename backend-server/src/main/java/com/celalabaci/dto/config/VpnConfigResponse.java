package com.celalabaci.dto.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Oluşturulan VPN konfigürasyon dosyasının içeriğini döndürmek için
 * kullanılan DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VpnConfigResponse {
    private String configurationFileContent;
    private String protocol;
    private String serverName;
}
