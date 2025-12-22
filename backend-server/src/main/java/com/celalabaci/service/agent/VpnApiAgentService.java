package com.celalabaci.service.agent;

import com.celalabaci.dto.agent.AgentDTOs;
import com.celalabaci.dto.agent.AgentDTOs.IkeV2Credentials;
import com.celalabaci.dto.agent.AgentDTOs.OpenVpnCredentials;
import com.celalabaci.dto.agent.AgentDTOs.V2RayCredentials;
import com.celalabaci.dto.agent.AgentDTOs.SuperCredentials;
import com.celalabaci.entity.Device;
import com.celalabaci.entity.User;
import com.celalabaci.entity.UserDevice;
import com.celalabaci.entity.VpnServer;

/**
 * VPN Sunucuları ile iletişim kuran, kullanıcıları/cihazları (peer) ekleyen,
 * silen veya sertifika üreten ara katman servisi (Agent) arayüzü.
 * * GÜNCELLEME: WireGuard metotları silindi. V2Ray ve Super metotları eklendi.
 */
public interface VpnApiAgentService {

    /**
     * Bir OpenVPN (Easy-RSA PKI) sunucusunda yeni bir kullanıcı sertifikası oluşturur.
     */
    OpenVpnCredentials provisionOpenVpnUser(VpnServer server, User user, UserDevice device);
    OpenVpnCredentials provisionOpenVpnGuest(VpnServer server, Device device);

    /**
     * Bir IKEv2 (strongSwan vb.) sunucusunda EAP kullanıcısı (secret) oluşturur.
     */
    IkeV2Credentials provisionIkeV2User(VpnServer server, User user, UserDevice device);
    IkeV2Credentials provisionIkeV2Guest(VpnServer server, Device device);

    /**
     * (YENİ) Bir V2Ray (X-UI vb.) sunucusunda yeni bir kullanıcı (inbound/client) oluşturur.
     * @return V2Ray bağlantı linki (vless://...) ve detayları.
     */
    V2RayCredentials provisionV2RayUser(VpnServer server, User user, UserDevice device);
    V2RayCredentials provisionV2RayGuest(VpnServer server, Device device);

    /**
     * (YENİ) Bir SUPER protokolü sunucusunda kullanıcı oluşturur.
     * @return SUPER bağlantı linki ve detayları.
     */
    SuperCredentials provisionSuperUser(VpnServer server, User user, UserDevice device);
    SuperCredentials provisionSuperGuest(VpnServer server, Device device);

    /**
     * Servisin erişilebilir olup olmadığını kontrol eder.
     */
    boolean ping();
}