package com.celalabaci.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; /**
 * OpenVPN (PKI) sunucusundan dönen sertifika bilgilerini temsil eder.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenVpnCredentials {
    private String caCert;              // CA Kök Sertifikası (public)
    private String userCert;            // Kullanıcıya özel üretilen sertifika (public)
    private String userKey;             // Kullanıcıya özel üretilen anahtar (private)
    private String tlsAuthKey;          // Sunucunun Tls-Auth anahtarı (public)
    private String serverProtocol;      // "udp" veya "tcp"
    private int serverPort;             // 1194
}
