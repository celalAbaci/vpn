package com.celalabaci.service.impl;

import com.celalabaci.dto.agent.AgentDTOs;
import com.celalabaci.dto.config.VpnConfigGenerationRequest;
import com.celalabaci.dto.config.VpnConfigResponse;
import com.celalabaci.dto.config.VpnProtocol;
import com.celalabaci.entity.Role;
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

import java.time.OffsetDateTime;

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
        boolean isGuest = false;

        // 1. Resolve User and Device
        if (currentUser != null) {
             // Authenticated User (Regular or Guest with Token)
             if (request.getDeviceId() != null) {
                 device = userDeviceRepository.findById(request.getDeviceId()).orElse(null);
             }
             if (currentUser.getRole() == Role.GUEST || currentUser.getUsername().startsWith("GUEST_")) {
                 isGuest = true;
             }
        } else if (request.getGuestDeviceId() != null) {
             // Unauthenticated Guest Request (Tokenless flow fallback)
             isGuest = true;
             String uniqueId = request.getGuestDeviceId();
             device = userDeviceRepository.findByUniqueDeviceId(uniqueId).orElse(null);

             // CRITICAL: Ensure device exists for logging requirements
             if (device == null) {
                 device = new UserDevice();
                 device.setUniqueDeviceId(uniqueId);
                 device.setDeviceName("Guest Device - " + uniqueId.substring(0, Math.min(uniqueId.length(), 8)));
                 device.setActive(true);
                 device.setLastSeen(OffsetDateTime.now());
                 device = userDeviceRepository.save(device);
             } else {
                 device.setLastSeen(OffsetDateTime.now());
                 userDeviceRepository.save(device);
             }
        }

        // If authenticated user is a guest but device wasn't linked or found by ID, try to find by unique ID if passed
        if (isGuest && device == null && request.getGuestDeviceId() != null) {
             device = userDeviceRepository.findByUniqueDeviceId(request.getGuestDeviceId()).orElse(null);
             // Create if missing (Hybrid flow safety)
             if (device == null) {
                 device = new UserDevice();
                 device.setUniqueDeviceId(request.getGuestDeviceId());
                 device.setDeviceName("Guest Device - " + request.getGuestDeviceId().substring(0, Math.min(request.getGuestDeviceId().length(), 8)));
                 device.setActive(true);
                 device.setLastSeen(OffsetDateTime.now());
                 // Link to user if currentUser is a real entity?
                 // If currentUser is transient (from GuestAuthController), we might not be able to save relation if User isn't in DB.
                 // Assuming Guest Auth Token User is transient. So we don't set user.
                 device = userDeviceRepository.save(device);
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
            sb.append("ignore-unknown-option shaper\n");
            sb.append("verb 3\n");

            // --- SPEED LIMIT LOGIC ---
            if (isGuest || (currentUser != null && currentUser.getRole() == Role.USER)) {
                // shaper 2000000 (Bytes per second) approx 16 Mbps
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
            logRecord.setUser(currentUser); // Nullable
            logRecord.setServer(entryServer);
            logRecord.setConfigContent(configContent);
            logRecord.setProtocol(protocol);
            logRecord.setActive(true);
            logRecord.setDevice(device); // Now reliably set

            userVpnConfigRepository.save(logRecord);
        } catch (Exception e) {
            log.error("Config loglanırken hata oluştu: " + e.getMessage());
        }

        return new VpnConfigResponse(configContent, protocol.name(), entryServer.getServerName());
    }
}
