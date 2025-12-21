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
import com.celalabaci.repository.UserConnectionLogRepository;
import com.celalabaci.repository.UserDeviceRepository;
import com.celalabaci.repository.UserVpnConfigRepository;
import com.celalabaci.repository.VpnServerRepository;
import com.celalabaci.service.IVpnConfigService;
import com.celalabaci.service.agent.VpnApiAgentService;
import java.math.BigDecimal;
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

        // Check for existing configuration first (Only for logged in users)
        if (currentUser != null) {
            var existingConfig = userVpnConfigRepository.findByUserIdAndServerIdAndProtocol(currentUser.getId(), entryServer.getId(), request.getProtocol());
            if (existingConfig.isPresent()) {
                return new VpnConfigResponse(existingConfig.get().getConfigContent(), request.getProtocol().name(), entryServer.getServerName());
            }
        }

        UserDevice device = null;
        if (currentUser != null) {
             device = userDeviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new ConfigGenerationException(MessageType.NO_RECORD_EXIST, "Cihaz bulunamadı"));
        } else {
             // Guest Logic
             device = userDeviceRepository.findById(request.getDeviceId()).orElse(null);
             if (device == null) {
                 throw new ConfigGenerationException(MessageType.NO_RECORD_EXIST, "Misafir cihaz kaydı bulunamadı. Lütfen önce cihazı kaydedin.");
             }
        }

        String configContent = "";

        switch (request.getProtocol()) {
            case OPENVPN:
                AgentDTOs.OpenVpnCredentials ovpn = vpnApiAgentService.provisionOpenVpnUser(entryServer, currentUser, device);

                // Start with the raw config from the server
                String rawConfig = ovpn.getUserCert();

                StringBuilder sb = new StringBuilder(rawConfig);

                // Ensure there is a newline before appending
                if (!rawConfig.endsWith("\n")) {
                    sb.append("\n");
                }

                // Add extra options that might be missing or needed
                sb.append("ignore-unknown-option block-outside-dns\n");

                // Guest hız limiti (Gerekirse)
                // 16Mbps approx (16*1000*1000 bits / 8 = 2000000 bytes)
                // Ancak OpenVPN 'shaper' byte/sec cinsinden çalışır.
                // 16 Mbit = ~2 MB/s = 2097152 bytes. 2000000 olarak bırakalım.
                if (currentUser == null) {
                     sb.append("shaper 2000000\n");
                }

                configContent = sb.toString();
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
        // Only save for logged-in users to allow re-use
        if (currentUser != null) {
            UserVpnConfig dbConfig = new UserVpnConfig();
            dbConfig.setUser(currentUser);
            dbConfig.setServer(entryServer);
            dbConfig.setProtocol(request.getProtocol());
            dbConfig.setConfigContent(configContent);
            // V2Ray ve Super için identifier olarak linkin bir parçasını veya UUID'yi kullanabiliriz
            userVpnConfigRepository.save(dbConfig);
        }

        return new VpnConfigResponse(configContent, request.getProtocol().name(), entryServer.getServerName());
    }
}