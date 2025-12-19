package com.abacicelal.supervpn_project.remote.model;

/**
 * Desteklenen DNS sağlayıcıları.
 * Backend'deki `com.celalabaci.dto.config.CustomDnsProvider` ile eşleşir.
 */
public enum CustomDnsProvider {
    DEFAULT,
    ADGUARD,
    CLOUDFLARE_SECURITY,
    CLOUDFLARE_FAMILY,
    QUAD9
}
