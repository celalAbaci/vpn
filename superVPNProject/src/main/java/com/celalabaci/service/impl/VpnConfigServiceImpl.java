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

        UserDevice device = null;
        if (currentUser != null && request.getDeviceId() != null) {
            device = userDeviceRepository.findById(request.getDeviceId()).orElse(null);
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

            // Hız Limiti: Premium olmayanlar (Guest/Free) için 16Mbps (2MB/s) limit
            boolean isPremium = currentUser != null && currentUser.getRole() == com.celalabaci.entity.Role.PREMIUM;
            if (!isPremium) {
                sb.append("shaper 2000000\n");
                sb.append("ignore-unknown-option shaper\n");
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