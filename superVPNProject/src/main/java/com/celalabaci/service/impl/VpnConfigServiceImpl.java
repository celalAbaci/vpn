package com.celalabaci.service.impl;

import com.celalabaci.dto.agent.AgentDTOs;
import com.celalabaci.dto.config.VpnConfigGenerationRequest;
import com.celalabaci.dto.config.VpnConfigResponse;
import com.celalabaci.entity.User;
import com.celalabaci.entity.UserDevice;
import com.celalabaci.entity.UserVpnConfig;
import com.celalabaci.entity.VpnServer;
import com.celalabaci.exception.ConfigGenerationException;
import com.celalabaci.exception.MessageType;
import com.celalabaci.repository.UserDeviceRepository;
import com.celalabaci.repository.UserVpnConfigRepository;
import com.celalabaci.repository.VpnServerRepository;
import com.celalabaci.service.IVpnConfigService;
import com.celalabaci.service.agent.VpnApiAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VpnConfigServiceImpl implements IVpnConfigService {

    private final VpnServerRepository vpnServerRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final VpnApiAgentService vpnApiAgentService;
    private final UserVpnConfigRepository userVpnConfigRepository;

    @Override
    public VpnConfigResponse generateConfig(VpnConfigGenerationRequest request, User currentUser) {
        VpnServer entryServer = vpnServerRepository.findById(request.getEntryServerId())
                .orElseThrow(() -> new ConfigGenerationException(MessageType.NO_RECORD_EXIST, "Sunucu bulunamadı"));

        UserDevice device = userDeviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new ConfigGenerationException(MessageType.NO_RECORD_EXIST, "Cihaz bulunamadı"));

        // Önce mevcut bir konfigürasyon var mı kontrol et
        java.util.Optional<UserVpnConfig> existingConfig = userVpnConfigRepository.findByUserIdAndServerIdAndProtocol(
                currentUser.getId(), entryServer.getId(), request.getProtocol());

        if (existingConfig.isPresent()) {
            return new VpnConfigResponse(existingConfig.get().getConfigContent(), request.getProtocol().name(), entryServer.getServerName());
        }

        String configContent = "";

        switch (request.getProtocol()) {
            case OPENVPN:
                AgentDTOs.OpenVpnCredentials ovpn = vpnApiAgentService.provisionOpenVpnUser(entryServer, currentUser, device);
                configContent = ovpn.getUserCert(); // Full OVPN içeriği
                break;

            case IKEV2:
                AgentDTOs.IkeV2Credentials ike = vpnApiAgentService.provisionIkeV2User(entryServer, currentUser, device);
                configContent = "Server: " + ike.getServerAddress() + "\n" +
                        "User: " + ike.getEapUsername() + "\n" +
                        "Pass: " + ike.getEapPassword();
                break;

            case V2RAY: // YENİ
                AgentDTOs.V2RayCredentials v2ray = vpnApiAgentService.provisionV2RayUser(entryServer, currentUser, device);
                // V2Ray için genellikle link (vless://...) konfigürasyon olarak kullanılır.
                configContent = v2ray.getConfigLink();
                break;

            case SUPER: // YENİ
                AgentDTOs.SuperCredentials spr = vpnApiAgentService.provisionSuperUser(entryServer, currentUser, device);
                configContent = spr.getSuperLink();
                break;

            default:
                throw new ConfigGenerationException(MessageType.GENERAL_EXCEPTION, "Desteklenmeyen protokol: " + request.getProtocol());
        }

        // --- VERİTABANINA KAYIT ---
        UserVpnConfig dbConfig = new UserVpnConfig();
        dbConfig.setUser(currentUser);
        dbConfig.setServer(entryServer);
        dbConfig.setProtocol(request.getProtocol());
        dbConfig.setConfigContent(configContent);
        // V2Ray ve Super için identifier olarak linkin bir parçasını veya UUID'yi kullanabiliriz
        userVpnConfigRepository.save(dbConfig);

        return new VpnConfigResponse(configContent, request.getProtocol().name(), entryServer.getServerName());
    }
}