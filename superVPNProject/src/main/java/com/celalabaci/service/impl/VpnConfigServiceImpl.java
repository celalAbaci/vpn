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

        // For guest, device might not be in DB or associated with User.
        // We need to fetch or create a placeholder device logic if strictly required by Agent Service.
        // But the previous analysis showed UserDevice requires User (nullable now).
        // If deviceId passed is just a random Long, it might fail.
        // For Guest, we might need to skip device lookup or look it up without user check.

        UserDevice device = null;
        if (currentUser != null) {
             device = userDeviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new ConfigGenerationException(MessageType.NO_RECORD_EXIST, "Cihaz bulunamadı"));
        } else {
             // Guest logic: Create a transient device object or handle in agent
             // Since Agent Service methods require device, we need to adapt.
             // We can check if device exists by ID, if not found and user is null, maybe create temporary?
             // Or rely on ID passed.
             // Since we modified UserDevice.user to be nullable, we can try to find it.
             device = userDeviceRepository.findById(request.getDeviceId()).orElse(null);
             if (device == null) {
                 // Register a temporary guest device entry
                 device = new UserDevice();
                 device.setDeviceName("Guest Device");
                 device.setUser(null);
                 // We need to save it to get an ID if needed, but ID is passed in request?
                 // Wait, request.getDeviceId() is the ID in DB.
                 // If the Android app generates a random ID, it won't exist in DB.
                 // The Android app should probably Register the device first even for Guest.
                 // But for now let's assume valid ID or return error.
                 // For now, let's create a dummy wrapper if null, but this might fail Hibernate.
                 throw new ConfigGenerationException(MessageType.NO_RECORD_EXIST, "Misafir cihaz kaydı bulunamadı. Lütfen önce cihazı kaydedin.");
             }
        }

        String configContent = "";

        switch (request.getProtocol()) {
            case OPENVPN:
                AgentDTOs.OpenVpnCredentials ovpn = vpnApiAgentService.provisionOpenVpnUser(entryServer, currentUser, device);

                // HATA DÜZELTMESİ: Tüm parçaları birleştirerek geçerli bir OVPN dosyası oluşturun
                StringBuilder sb = new StringBuilder();
                sb.append("client\n");
                sb.append("dev tun\n");
                sb.append("proto ").append(ovpn.getServerProtocol() != null ? ovpn.getServerProtocol() : "udp").append("\n");
                sb.append("remote ").append(entryServer.getServerIpAddress()).append(" ").append(ovpn.getServerPort()).append("\n");
                sb.append("resolv-retry infinite\n");
                sb.append("nobind\n");
                sb.append("persist-key\n");
                sb.append("persist-tun\n");
                sb.append("remote-cert-tls server\n");
                sb.append("cipher AES-256-CBC\n"); // Sunucu ayarınıza göre değişebilir
                sb.append("auth SHA512\n");        // Sunucu ayarınıza göre değişebilir
                sb.append("verb 3\n");

                // CA Sertifikası
                sb.append("<ca>\n").append(ovpn.getCaCert()).append("\n</ca>\n");

                // Kullanıcı Sertifikası
                sb.append("<cert>\n").append(ovpn.getUserCert()).append("\n</cert>\n");

                // Kullanıcı Özel Anahtarı
                sb.append("<key>\n").append(ovpn.getUserKey()).append("\n</key>\n");

                // TLS Auth/Crypt Anahtarı (varsa)
                if (ovpn.getTlsAuthKey() != null && !ovpn.getTlsAuthKey().isEmpty()) {
                    // tls-crypt mi tls-auth mu kullandığınız sunucuya bağlıdır, SQL örneğinizde tls-crypt var.
                    sb.append("<tls-crypt>\n").append(ovpn.getTlsAuthKey()).append("\n</tls-crypt>\n");
                }

                configContent = sb.toString();

                // GUEST LIMITS for OpenVPN
                if (currentUser == null) {
                    // Inject speed limit (shaper)
                    // 10 Mbps = 10485760 bits/sec approx. OpenVPN 'shaper' uses bytes.
                    // 10 Mbits/s ~= 1.25 MB/s = 1250000 bytes.
                    // shaper 1250000
                    configContent += "\nshaper 1250000\n";
                }
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