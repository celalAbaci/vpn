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
        if (request.getDeviceId() != null) {
            device = userDeviceRepository.findById(request.getDeviceId()).orElse(null);
        }

        // Misafir kullanıcılar için unique ID'den cihazı bul
        if (device == null && currentUser != null && currentUser.getUsername().startsWith("GUEST_")) {
            String uniqueId = currentUser.getUsername().substring(6); // "GUEST_" prefixini at
            device = userDeviceRepository.findByUniqueDeviceId(uniqueId).orElse(null);
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
            // Shaper (Hız Limiti) Eklemesi
            // Premium olmayan (User, Guest) herkese limit uygula
            boolean isPremium = currentUser != null && com.celalabaci.entity.Role.PREMIUM.equals(currentUser.getRole());
            if (!isPremium) {
                sb.append("ignore-unknown-option shaper\n");
                sb.append("shaper 2000000\n"); // 2MB/s (~16Mbps)
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
             // Misafir kullanıcıların (currentUser=null olmasa da gerçek DB user'ı değil) loglanması
             // User null olabilir (Guest için), ama cihaz varsa loglayalım.
            UserVpnConfig logRecord = new UserVpnConfig();

            // Eğer GUEST ise user null set edilebilir veya transient user.
            // Entity'de user nullable yaptık.
            // Transient user JPA hatası verebilir (unsaved instance).
            // O yüzden GUEST ise null geçiyoruz.
            if (currentUser != null && currentUser.getUsername().startsWith("GUEST_")) {
                logRecord.setUser(null);
            } else {
                logRecord.setUser(currentUser);
            }

            logRecord.setDevice(device);
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