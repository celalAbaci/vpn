package com.celalabaci.service.impl;

import com.celalabaci.dto.agent.AgentDTOs;
import com.celalabaci.dto.config.VpnConfigGenerationRequest;
import com.celalabaci.dto.config.VpnConfigResponse;
import com.celalabaci.entity.Device;
import com.celalabaci.entity.Role;
import com.celalabaci.entity.User;
import com.celalabaci.entity.UserDevice;
import com.celalabaci.entity.UserVpnConfig;
import com.celalabaci.entity.VpnServer;
import com.celalabaci.exception.ConfigGenerationException;
import com.celalabaci.exception.MessageType;
import com.celalabaci.repository.DeviceRepository;
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
    private final DeviceRepository deviceRepository;
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

        UserDevice userDevice = null;
        Device guestDevice = null;

        if (currentUser != null) {
            // Logged-in User
            if (request.getDeviceId() == null) {
                 throw new ConfigGenerationException(MessageType.VALIDATION_ERROR, "Cihaz ID (deviceId) zorunludur.");
            }
            userDevice = userDeviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new ConfigGenerationException(MessageType.NO_RECORD_EXIST, "Cihaz bulunamadı"));
        } else {
             // Guest User
             if (request.getGuestDeviceId() == null) {
                 throw new ConfigGenerationException(MessageType.VALIDATION_ERROR, "Misafir Cihaz ID (guestDeviceId) zorunludur.");
             }
             guestDevice = deviceRepository.findByUniqueDeviceId(request.getGuestDeviceId()).orElse(null);
             if (guestDevice == null) {
                 throw new ConfigGenerationException(MessageType.NO_RECORD_EXIST, "Misafir cihaz kaydı bulunamadı. Lütfen önce cihazı kaydedin.");
             }
        }

        String configContent = "";

        // Speed Limit for Guest & Free Users (16Mbps = 2MB/s = 2000000 bytes)
        // Check if user is null (Guest) or Role is not PREMIUM
        boolean isFreeTier = currentUser == null || (currentUser.getRole() != Role.PREMIUM && currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.MODERATOR);

        switch (request.getProtocol()) {
            case OPENVPN:
                AgentDTOs.OpenVpnCredentials ovpn;
                if (currentUser != null) {
                    ovpn = vpnApiAgentService.provisionOpenVpnUser(entryServer, currentUser, userDevice);
                } else {
                    ovpn = vpnApiAgentService.provisionOpenVpnGuest(entryServer, guestDevice);
                }

                String rawConfig = ovpn.getUserCert();
                StringBuilder sb = new StringBuilder(rawConfig);
                if (!rawConfig.endsWith("\n")) sb.append("\n");

                sb.append("ignore-unknown-option block-outside-dns\n");
                sb.append("ignore-unknown-option shaper\n");

                // Enforce speed limit for non-premium users
                if (isFreeTier) {
                     sb.append("shaper 2000000\n");
                }

                configContent = sb.toString();
                break;

            case IKEV2:
                AgentDTOs.IkeV2Credentials ike;
                if (currentUser != null) {
                     ike = vpnApiAgentService.provisionIkeV2User(entryServer, currentUser, userDevice);
                } else {
                     ike = vpnApiAgentService.provisionIkeV2Guest(entryServer, guestDevice);
                }
                configContent = "Server: " + ike.getServerAddress() + "\n" +
                        "User: " + ike.getEapUsername() + "\n" +
                        "Pass: " + ike.getEapPassword();
                break;

            case V2RAY:
                 AgentDTOs.V2RayCredentials v2ray;
                 if (currentUser != null) {
                     v2ray = vpnApiAgentService.provisionV2RayUser(entryServer, currentUser, userDevice);
                 } else {
                     v2ray = vpnApiAgentService.provisionV2RayGuest(entryServer, guestDevice);
                 }
                configContent = v2ray.getConfigLink();
                break;

            case SUPER:
                AgentDTOs.SuperCredentials spr;
                if (currentUser != null) {
                    spr = vpnApiAgentService.provisionSuperUser(entryServer, currentUser, userDevice);
                } else {
                    spr = vpnApiAgentService.provisionSuperGuest(entryServer, guestDevice);
                }
                configContent = spr.getSuperLink();
                break;

            default:
                throw new ConfigGenerationException(MessageType.GENERAL_EXCEPTION, "Desteklenmeyen protokol: " + request.getProtocol());
        }

        // --- VERİTABANINA KAYIT (Sadece Logged-in) ---
        if (currentUser != null) {
            UserVpnConfig dbConfig = new UserVpnConfig();
            dbConfig.setUser(currentUser);
            dbConfig.setServer(entryServer);
            dbConfig.setProtocol(request.getProtocol());
            dbConfig.setConfigContent(configContent);
            userVpnConfigRepository.save(dbConfig);
        }

        return new VpnConfigResponse(configContent, request.getProtocol().name(), entryServer.getServerName());
    }
}
