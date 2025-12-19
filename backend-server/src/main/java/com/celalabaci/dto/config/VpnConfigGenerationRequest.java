package com.celalabaci.dto.config;

import com.celalabaci.dto.config.VpnProtocol;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Kullanıcının hangi sunucu ve hangi protokol için konfigürasyon istediğini
 * belirtmek için kullanılan DTO.
 * Multi-Hop (Double VPN) desteği için güncellendi.
 * YENİ: CustomDnsProvider desteği eklendi.
 */
@Data
public class VpnConfigGenerationRequest {

    /**
     * Giriş (Entry) sunucusunun ID'si.
     * Single-Hop (normal) bağlantılarda bu tek sunucudur.
     * Multi-Hop bağlantılarda bu, istemcinin bağlanacağı ilk sunucudur.
     */
    @NotNull(message = "Giriş Sunucusu ID'si (entryServerId) boş olamaz.")
    private Long entryServerId;

    /**
     * (YENİ) Çıkış (Exit) sunucusunun ID'si.
     * Burası null ise, normal (Single-Hop) bir konfigürasyon oluşturulur.
     * Burası dolu ise, Multi-Hop (Double VPN) konfigürasyonu denenir
     * (örn: Entry -> Exit -> İnternet).
     */
    private Long exitServerId; // Opsiyonel

    @NotNull(message = "Protokol tipi boş olamaz.")
    private VpnProtocol protocol;

    @NotNull(message = "Cihaz ID'si boş olamaz.")
    private Long deviceId;

    /**
     * (YENİ EKLENDİ)
     * Kullanıcının talep ettiği özel DNS sağlayıcısı.
     * Eğer bu alan 'null' gelirse, sistem varsayılan (DEFAULT) DNS'i kullanır.
     */
    private CustomDnsProvider dnsProvider;
}
