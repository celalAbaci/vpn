package com.celalabaci.service.agent;

import com.celalabaci.dto.agent.AgentDTOs;
import com.celalabaci.dto.agent.AgentDTOs.IkeV2Credentials;
import com.celalabaci.dto.agent.AgentDTOs.OpenVpnCredentials;
import com.celalabaci.dto.agent.AgentDTOs.V2RayCredentials;
import com.celalabaci.dto.agent.AgentDTOs.SuperCredentials;
import com.celalabaci.entity.User;
import com.celalabaci.entity.UserDevice;
import com.celalabaci.entity.VpnServer;
import com.celalabaci.exception.ConfigGenerationException;
import com.celalabaci.exception.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VpnApiAgentServiceImpl implements VpnApiAgentService {

    private final ISshAgentService sshAgentService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ----------------------------------------------------------------
    // 1. OPENVPN ENTEGRASYONU (Native Script)
    // ----------------------------------------------------------------
    @Override
    public OpenVpnCredentials provisionOpenVpnUser(VpnServer server, User user, UserDevice device) {
        String clientName = user.getUsername() + "_" + device.getId();

        // Komut: MENU_OPTION="1" CLIENT="testuser" PASS="1" ./openvpn-install.sh
        String createCommand = String.format(
                "MENU_OPTION=\"1\" CLIENT=\"%s\" PASS=\"1\" ./openvpn-install.sh",
                clientName
        );
        String catCommand = String.format("cat /root/%s.ovpn", clientName);
        String cleanCommand = String.format("rm /root/%s.ovpn", clientName);

        try {
            sshAgentService.connect(server.getServerIpAddress(), server.getSshUsername(), server.getSshPassword());

            log.info("OpenVPN kullanıcısı oluşturuluyor: {}", clientName);
            sshAgentService.runCommand(createCommand);

            log.info("OpenVPN dosyası okunuyor...");
            String configContent = sshAgentService.runCommand(catCommand);

            if (configContent == null || configContent.isEmpty()) {
                throw new ConfigGenerationException(MessageType.AGENT_PROVISIONING_FAILED, "OpenVPN dosyası boş döndü.");
            }

            // Temizlik
            sshAgentService.runCommand(cleanCommand);

            return new OpenVpnCredentials(
                    "", // CA gömülü
                    configContent, // FULL CONFIG
                    "", // Key gömülü
                    "", // TLS gömülü
                    "udp",
                    1194
            );
        } catch (Exception e) {
            log.error("OpenVPN Hatası: {}", e.getMessage());
            throw new ConfigGenerationException(MessageType.AGENT_PROVISIONING_FAILED, "OpenVPN kurulum hatası: " + e.getMessage());
        } finally {
            sshAgentService.disconnect();
        }
    }

    // ----------------------------------------------------------------
    // 2. IKEv2 ENTEGRASYONU
    // ----------------------------------------------------------------
    @Override
    public IkeV2Credentials provisionIkeV2User(VpnServer server, User user, UserDevice device) {
        String username = user.getUsername() + device.getId();
        String password = UUID.randomUUID().toString().substring(0, 10);

        String addCommand = String.format(
                "docker exec -i dataguard-ikev2 sh -c \"echo '%s : EAP \\\"%s\\\"' >> /etc/ipsec.secrets\"",
                username, password
        );
        String reloadCommand = "docker exec -i dataguard-ikev2 ipsec secrets";

        try {
            sshAgentService.connect(server.getServerIpAddress(), server.getSshUsername(), server.getSshPassword());

            log.info("IKEv2 kullanıcısı ekleniyor: {}", username);
            sshAgentService.runCommand(addCommand);

            log.info("IKEv2 reload yapılıyor...");
            sshAgentService.runCommand(reloadCommand);

            return new IkeV2Credentials(
                    username,
                    password,
                    server.getServerIpAddress(),
                    server.getServerIpAddress()
            );
        } catch (Exception e) {
            throw new ConfigGenerationException(MessageType.AGENT_PROVISIONING_FAILED, "IKEv2 hatası: " + e.getMessage());
        } finally {
            sshAgentService.disconnect();
        }
    }

    // ----------------------------------------------------------------
    // 3. V2RAY ENTEGRASYONU (YENİ - X-UI Panel API Örneği)
    // ----------------------------------------------------------------
    @Override
    public V2RayCredentials provisionV2RayUser(VpnServer server, User user, UserDevice device) {
        // X-UI API Endpoints
        String baseUrl = "http://" + server.getServerIpAddress() + ":" + server.getAdminApiPort(); // örn: 2053
        String loginUrl = baseUrl + "/login";
        String addClientUrl = baseUrl + "/panel/api/inbounds/addClient";

        // NOT: Gerçek V2Ray implementasyonunda Inbound ID bilinmelidir.
        // Burada örnek olarak Inbound ID = 1 varsayıyoruz veya yeni inbound oluşturulur.

        try {
            // 1. Login Ol (Cookie almak için)
            // Bu örnek basitleştirilmiştir, gerçekte RestTemplate ile cookie yönetimi yapılmalıdır.
            // X-UI genellikle session cookie kullanır.

            String clientUuid = UUID.randomUUID().toString();
            String email = user.getUsername() + "_" + device.getId() + "@supervpn.com";

            // VLESS Link Oluşturma (Manuel String Manipülasyonu - Örnektir)
            // vless://UUID@IP:PORT?security=reality&sni=google.com&fp=chrome&pbk=...&sid=...&type=tcp&headerType=none#ISIM
            String vlessLink = String.format(
                    "vless://%s@%s:443?security=reality&encryption=none&type=tcp&headerType=none#%s",
                    clientUuid,
                    server.getServerIpAddress(),
                    user.getUsername()
            );

            log.info("V2Ray kullanıcısı (sanal) oluşturuldu: {}", email);

            // Gerçek API çağrısı yerine şimdilik mock dönüş yapıyoruz
            // çünkü sunucu tarafında X-UI kurulu olup olmadığını bilmiyoruz.

            return new V2RayCredentials(
                    vlessLink,
                    "{\"v\": \"2\", \"ps\": \"" + user.getUsername() + "\", \"add\": \"" + server.getServerIpAddress() + "\", \"port\": \"443\", \"id\": \"" + clientUuid + "\", \"net\": \"tcp\", \"type\": \"none\", \"tls\": \"none\"}",
                    clientUuid
            );

        } catch (Exception e) {
            log.error("V2Ray API hatası: {}", e.getMessage());
            throw new ConfigGenerationException(MessageType.AGENT_PROVISIONING_FAILED, "V2Ray Hatası: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // 4. SUPER ENTEGRASYONU (YENİ)
    // ----------------------------------------------------------------
    @Override
    public SuperCredentials provisionSuperUser(VpnServer server, User user, UserDevice device) {
        // SUPER protokolü için özel implementasyon.
        // Örnek olarak basit bir link üretimi.

        String secretKey = UUID.randomUUID().toString().replace("-", "");
        String superLink = String.format(
                "super://%s@%s:%d?name=%s",
                secretKey,
                server.getServerIpAddress(),
                443, // Varsayılan port
                user.getUsername()
        );

        log.info("SUPER protokolü kullanıcısı oluşturuldu: {}", user.getUsername());

        return new SuperCredentials(
                superLink,
                secretKey,
                server.getServerIpAddress()
        );
    }

    @Override
    public boolean ping() {
        return true;
    }
}