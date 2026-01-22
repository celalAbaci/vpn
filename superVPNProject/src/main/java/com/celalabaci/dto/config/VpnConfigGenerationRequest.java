package com.celalabaci.dto.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VpnConfigGenerationRequest {
    private Long entryServerId;
    private Long deviceId; // Database ID for registered users
    private String guestDeviceId; // Unique String ID for guests
    private VpnProtocol protocol;
    private CustomDnsProvider dnsProvider;
    private Long exitServerId;
}
