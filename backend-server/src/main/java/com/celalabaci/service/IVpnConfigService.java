package com.celalabaci.service;

import com.celalabaci.dto.config.VpnConfigGenerationRequest;
import com.celalabaci.dto.config.VpnConfigResponse;
import com.celalabaci.entity.User;

/**
 * VPN konfigürasyon dosyalarını oluşturmaktan sorumlu servis arayüzü.
 */
public interface IVpnConfigService {

    /**
     * Kullanıcının isteğine, sunucu durumuna ve abonelik durumuna göre
     * bir VPN konfigürasyon dosyası oluşturur.
     *
     * @param request     Kullanıcının hangi sunucu, cihaz ve protokolü istediğini belirten DTO.
     * @param currentUser İsteği yapan, JWT ile doğrulanmış kullanıcı.
     * @return Oluşturulan konfigürasyon dosyasını içeren bir VpnConfigResponse nesnesi.
     */
    VpnConfigResponse generateConfig(VpnConfigGenerationRequest request, User currentUser);
}
