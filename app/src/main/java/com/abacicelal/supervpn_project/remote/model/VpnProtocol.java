package com.abacicelal.supervpn_project.remote.model;

/**
 * Desteklenen VPN protokolleri.
 * Backend'deki `com.celalabaci.dto.config.VpnProtocol` ile eşleşir.
 */
public enum VpnProtocol {
    AUTO,    // İstemci tarafında çözümlenir — backend'e gönderilmez
    OPENVPN,
    IKEV2,
    V2RAY,
    SUPER,
    XRAY
}
