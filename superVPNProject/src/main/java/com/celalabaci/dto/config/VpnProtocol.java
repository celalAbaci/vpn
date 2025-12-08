package com.celalabaci.dto.config;

/**
 * Desteklenen VPN protokollerini tanımlayan enum.
 * GÜNCELLEME: WIREGUARD kaldırıldı, V2RAY ve SUPER eklendi.
 */
public enum VpnProtocol {
    OPENVPN,
    IKEV2,
    V2RAY, // YENİ
    SUPER  // YENİ
}