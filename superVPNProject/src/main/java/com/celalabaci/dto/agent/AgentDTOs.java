package com.celalabaci.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * VpnApiAgentService'in VPN sunucularından döndürdüğü
 * kimlik bilgilerini taşımak için kullanılan DTO (Data Transfer Object) sınıfları.
 * * GÜNCELLEME: WireGuardPeer kaldırıldı. V2Ray ve Super için sınıflar eklendi.
 */
public class AgentDTOs {

    /**
     * OpenVPN (PKI) sunucusundan dönen sertifika bilgilerini temsil eder.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpenVpnCredentials {
        private String caCert;       // CA Kök Sertifikası (public)
        private String userCert;     // Kullanıcıya özel üretilen sertifika (public)
        private String userKey;      // Kullanıcıya özel üretilen anahtar (private)
        private String tlsAuthKey;   // Sunucunun Tls-Auth anahtarı (public)
        private String serverProtocol; // "udp" veya "tcp"
        private int serverPort;      // 1194
    }

    /**
     * IKEv2 (EAP-MSCHAPv2) sunucusundan dönen kullanıcı bilgilerini temsil eder.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IkeV2Credentials {
        private String eapUsername;
        private String eapPassword;
        private String serverAddress; // Sunucu IP veya domain adı
        private String remoteId;      // Sunucu Remote ID (genellikle IP adresi)
    }

    /**
     * V2Ray (VLESS/VMESS) sunucusundan dönen bilgileri temsil eder.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class V2RayCredentials {
        private String configLink;   // vless://... veya vmess://... formatında link
        private String configJson;   // İstemciye JSON olarak verilmesi gerekirse
        private String uuid;         // Kullanıcı UUID'si
    }

    /**
     * SUPER protokolü sunucusundan dönen bilgileri temsil eder.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuperCredentials {
        private String superLink;    // super://... formatında link
        private String secretKey;    // Protokole özel gizli anahtar
        private String serverAddress;
    }
}