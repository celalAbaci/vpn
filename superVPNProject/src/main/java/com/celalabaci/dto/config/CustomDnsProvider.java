package com.celalabaci.dto.config;

import lombok.Getter;

/**
 * Desteklenen özelleştirilmiş DNS sağlayıcılarını ve IP adreslerini tanımlayan enum.
 * Konfigürasyon üretme adımında kullanılacaktır.
 */
@Getter
public enum CustomDnsProvider {

    // Varsayılan (Cloudflare Public)
    DEFAULT("1.1.1.1, 1.0.0.1"),

    // Güvenlik ve Reklam Engelleme
    ADGUARD("94.140.14.14, 94.140.15.15"),

    // Sadece Güvenlik (Malware Engelleme)
    CLOUDFLARE_SECURITY("1.1.1.2, 1.0.0.2"),

    // Güvenlik ve Yetişkin İçerik Engelleme
    CLOUDFLARE_FAMILY("1.1.1.3, 1.0.0.3"),

    // Güvenlik (Malware, Phishing Engelleme)
    QUAD9("9.9.9.9, 149.112.112.112");

    private final String dnsString;

    CustomDnsProvider(String dnsString) {
        this.dnsString = dnsString;
    }

    /**
     * DNS IP'lerini yapılandırma dosyasına eklenecek formatta (virgülle ayrılmış) döndürür.
     * @return DNS IP adresleri.
     */
    public String getDnsString() {
        return dnsString;
    }
}
