package com.abacicelal.supervpn_project.remote.model;

/**
 * Desteklenen VPN protokolleri.
 * Backend'deki `com.celalabaci.dto.config.VpnProtocol` ile eşleşir.
 */
public enum VpnProtocol {
    OPENVPN,
    WIREGUARD,
    IKEV2
}
