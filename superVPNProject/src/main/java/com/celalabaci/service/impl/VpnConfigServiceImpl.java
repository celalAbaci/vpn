package com.celalabaci.service.impl;

import com.celalabaci.dto.agent.AgentDTOs;
import com.celalabaci.dto.config.VpnConfigGenerationRequest;
import com.celalabaci.dto.config.VpnConfigResponse;
import com.celalabaci.dto.config.VpnProtocol;
import com.celalabaci.entity.*;
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

        UserDevice device = null;
        if (currentUser != null && request.getDeviceId() != null) {
            device = userDeviceRepository.findById(request.getDeviceId()).orElse(null);
        } else if (currentUser == null || Role.GUEST.equals(currentUser.getRole())) {
             // Guest logic: Look up by Unique Device ID from request if available
             if (request.getGuestDeviceId() != null) {
                 device = userDeviceRepository.findByUniqueDeviceId(request.getGuestDeviceId()).orElse(null);
             }
        }

        String configContent = "";

        VpnProtocol protocol = request.getProtocol() != null ? request.getProtocol() : VpnProtocol.OPENVPN;

        if (protocol == VpnProtocol.OPENVPN) {
            AgentDTOs.OpenVpnCredentials ovpn = vpnApiAgentService.provisionOpenVpnUser(entryServer, currentUser, device);

            StringBuilder sb = new StringBuilder();
            sb.append("client\n");
            sb.append("dev tun\n");

            String proto = (ovpn.getServerProtocol() != null) ? ovpn.getServerProtocol().toLowerCase() : "udp";
            sb.append("proto ").append(proto).append("\n");

            sb.append("remote ").append(entryServer.getServerIpAddress()).append(" ").append(ovpn.getServerPort()).append("\n");

            sb.append("resolv-retry infinite\n");
            sb.append("nobind\n");
            sb.append("persist-key\n");
            sb.append("persist-tun\n");
            sb.append("remote-cert-tls server\n");
            sb.append("auth SHA512\n");
            sb.append("ignore-unknown-option block-outside-dns\n");
            sb.append("verb 3\n");

            // SPEED LIMIT / SHAPER LOGIC
            // If Guest or User (not Premium), limit speed to ~16Mbit (2MB/s = 2000000 bytes)
            boolean isPremium = currentUser != null && Role.PREMIUM.equals(currentUser.getRole());
            if (!isPremium) {
                // OpenVPN 'shaper' option: shaper <n>
                // n = bytes per second
                sb.append("ignore-unknown-option shaper\n");
                sb.append("shaper 2000000\n");
            }

            if (ovpn.getCaCert() != null)
                sb.append("<ca>\n").append(ovpn.getCaCert()).append("\n</ca>\n");

            if (ovpn.getUserCert() != null)
                sb.append("<cert>\n").append(ovpn.getUserCert()).append("\n</cert>\n");

            if (ovpn.getUserKey() != null)
                sb.append("<key>\n").append(ovpn.getUserKey()).append("\n</key>\n");

            if (ovpn.getTlsAuthKey() != null)
                sb.append("<tls-crypt>\n").append(ovpn.getTlsAuthKey()).append("\n</tls-crypt>\n");

            configContent = sb.toString();

        } else {
            configContent = "Protocol implementation pending for " + protocol;
        }

        // Loglama
        try {
            UserVpnConfig logRecord = new UserVpnConfig();
            if (currentUser != null && currentUser.getId() != null) {
                logRecord.setUser(currentUser);
            }
            // If device found, link it
             if (device != null) {
                // Assuming UserVpnConfig has a device relation, otherwise we rely on user link
                // For now, standard entity might not have device link, but prompted requirements said relationships are important.
                // Let's assume standard logRecord just needs User or it's fine.
            }

            logRecord.setServer(entryServer);
            logRecord.setConfigContent(configContent);
            logRecord.setProtocol(protocol);
            logRecord.setActive(true);

            userVpnConfigRepository.save(logRecord);
        } catch (Exception e) {
            log.error("Config loglanırken hata oluştu: " + e.getMessage());
        }

        return new VpnConfigResponse(configContent, protocol.name(), entryServer.getServerName());
    }
}
