package com.celalabaci.service.impl;

import com.celalabaci.dto.agent.AgentDTOs;
import com.celalabaci.dto.config.VpnConfigGenerationRequest;
import com.celalabaci.dto.config.VpnConfigResponse;
import com.celalabaci.dto.config.VpnProtocol;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

        // Device Lookup Logic
        UserDevice device = null;
        boolean isGuest = false;

        if (request.getGuestDeviceId() != null) {
            // Guest User Lookup
            device = userDeviceRepository.findByUniqueDeviceId(request.getGuestDeviceId()).orElse(null);
            isGuest = true;
        } else if (currentUser != null && request.getDeviceId() != null) {
            // Logged-in User Lookup (Standard)
            // If currentUser is our transient Guest user (GUEST_ prefix), we might not find device by ID if user ID is null in DB
            // But usually logged in means via Token, which has ID if it's a real user.
            // If it's a guest token, it has "GUEST_xxx" username.
            if (currentUser.getUsername().startsWith("GUEST_")) {
                isGuest = true;
                // Try to find by unique ID if passed, or extract from username?
                // Ideally request should send guestDeviceId.
            } else {
                device = userDeviceRepository.findById(request.getDeviceId()).orElse(null);
            }
        }

        // Check Access Rights
        if (isGuest && !entryServer.isFree()) {
             throw new ConfigGenerationException(MessageType.GENERAL_EXCEPTION, "Guest users can only access Free servers.");
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
            sb.append("ignore-unknown-option shaper\n"); // Ensure Android client ignores it if not supported

            // Speed Limit Logic
            // 16 Mbit = 2 MB/s = 2000000 Bytes/s
            if (isGuest || (currentUser != null && currentUser.getRole() == com.celalabaci.entity.Role.USER)) {
                // Free users and Guests get limited
                sb.append("shaper 2000000\n");
            }

            sb.append("verb 3\n");

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
            if (currentUser != null) {
                UserVpnConfig logRecord = new UserVpnConfig();
                logRecord.setUser(currentUser);
                logRecord.setServer(entryServer);
                logRecord.setConfigContent(configContent);

                // Entity'de 'protocol' alanı olduğu için bunu tekrar ekliyoruz
                logRecord.setProtocol(protocol);

                // UserVpnConfig sınıfına 'active' alanını eklediğimiz için bu artık çalışacak
                logRecord.setActive(true);

                userVpnConfigRepository.save(logRecord);
            }
        } catch (Exception e) {
            log.error("Config loglanırken hata oluştu: " + e.getMessage());
        }

        return new VpnConfigResponse(configContent, protocol.name(), entryServer.getServerName());
    }
}