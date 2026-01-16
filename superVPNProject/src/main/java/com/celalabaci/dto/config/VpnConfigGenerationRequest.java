package com.celalabaci.dto.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VpnConfigGenerationRequest {
    // Android bu ismi gönderiyor, Long olmalı
    private Long entryServerId;

    // Android bu ismi gönderiyor, Long olmalı
    private Long deviceId;

    // Android "OPENVPN" gönderiyor, Enum ile eşleşmeli
    private VpnProtocol protocol;

    // Opsiyonel alanlar (Android göndermese de sorun olmaz)
    private CustomDnsProvider dnsProvider;
    private Long exitServerId;
    private String guestDeviceId; // For guest lookups
}