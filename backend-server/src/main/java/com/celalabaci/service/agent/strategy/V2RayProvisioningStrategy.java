package com.celalabaci.service.agent.strategy;

import com.celalabaci.dto.agent.AgentDTOs;
import com.celalabaci.dto.config.VpnProtocol;
import com.celalabaci.entity.User;
import com.celalabaci.entity.UserDevice;
import com.celalabaci.entity.VpnServer;
import com.celalabaci.exception.ConfigGenerationException;
import com.celalabaci.exception.MessageType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class V2RayProvisioningStrategy implements VpnProvisioningStrategy {

    // We instantiate these manually to avoid missing bean definition issues
    // since the project might not have a global RestTemplate bean configured.
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public VpnProtocol getProtocol() {
        return VpnProtocol.V2RAY;
    }

    @Override
    public AgentDTOs.V2RayCredentials provision(VpnServer server, User user, UserDevice device) {
        String baseUrl = "http://" + server.getServerIpAddress() + ":" + server.getAdminApiPort();
        String loginUrl = baseUrl + "/login";
        String addClientUrl = baseUrl + "/panel/api/inbounds/addClient";
        String listInboundsUrl = baseUrl + "/panel/api/inbounds/list";

        String xuiUsername = server.getSshUsername();
        String xuiPassword = server.getSshPassword();

        log.warn("Using SSH credentials for X-UI panel login. Ensure these match or update VpnServer entity to support separate credentials.");

        try {
            // 1. LOGIN
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("username", xuiUsername);
            map.add("password", xuiPassword);

            HttpEntity<MultiValueMap<String, String>> loginRequest = new HttpEntity<>(map, headers);
            ResponseEntity<String> loginResponse = restTemplate.postForEntity(loginUrl, loginRequest, String.class);

            if (!loginResponse.getStatusCode().is2xxSuccessful()) {
                throw new ConfigGenerationException(MessageType.AGENT_PROVISIONING_FAILED, "X-UI Login failed. Check credentials.");
            }

            // Extract Cookies
            String cookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.add(HttpHeaders.COOKIE, cookie);
            authHeaders.setContentType(MediaType.APPLICATION_JSON);

            // 2. FIND INBOUND
            HttpEntity<Void> listRequest = new HttpEntity<>(authHeaders);
            ResponseEntity<String> listResponse = restTemplate.exchange(listInboundsUrl, HttpMethod.GET, listRequest, String.class);

            int inboundId = -1;
            if (listResponse.getStatusCode().is2xxSuccessful() && listResponse.getBody() != null) {
                JsonNode root = objectMapper.readTree(listResponse.getBody());
                if (root.has("obj")) {
                    for (JsonNode node : root.get("obj")) {
                        if (node.has("protocol") && "vless".equalsIgnoreCase(node.get("protocol").asText())) {
                            inboundId = node.get("id").asInt();
                            break;
                        }
                    }
                }
            }

            if (inboundId == -1) {
                log.warn("Could not find a VLESS inbound via API. Falling back to ID=1.");
                inboundId = 1;
            }

            // 3. ADD CLIENT
            String clientUuid = UUID.randomUUID().toString();
            String email = user.getUsername() + "_" + device.getId();

            Map<String, Object> clientMap = new HashMap<>();
            clientMap.put("id", clientUuid);
            clientMap.put("alterId", 0);
            clientMap.put("email", email);
            clientMap.put("limitIp", 0);
            clientMap.put("totalFlow", 0);
            clientMap.put("expiryTime", 0);
            clientMap.put("enable", true);
            clientMap.put("tgId", "");
            clientMap.put("subId", "");

            String clientSettingsJson = objectMapper.writeValueAsString(clientMap);

            Map<String, Object> payload = new HashMap<>();
            payload.put("id", inboundId);
            payload.put("settings", clientSettingsJson);

            HttpEntity<Map<String, Object>> addClientReq = new HttpEntity<>(payload, authHeaders);
            ResponseEntity<String> addResponse = restTemplate.postForEntity(addClientUrl, addClientReq, String.class);

            if (!addResponse.getStatusCode().is2xxSuccessful()) {
                 log.error("X-UI Add Client failed: {}", addResponse.getBody());
                 throw new ConfigGenerationException(MessageType.AGENT_PROVISIONING_FAILED, "X-UI Add Client failed.");
            }

            JsonNode addRespNode = objectMapper.readTree(addResponse.getBody());
            if (!addRespNode.path("success").asBoolean(true)) {
                 log.error("X-UI returned failure: {}", addResponse.getBody());
            }

            // 4. GENERATE LINK
            String vlessLink = String.format(
                    "vless://%s@%s:443?security=reality&encryption=none&type=tcp&headerType=none#%s",
                    clientUuid,
                    server.getServerIpAddress(),
                    email
            );

            return new AgentDTOs.V2RayCredentials(
                    vlessLink,
                    clientSettingsJson,
                    clientUuid
            );

        } catch (Exception e) {
            log.error("V2Ray API Error: {}", e.getMessage());
            throw new ConfigGenerationException(MessageType.AGENT_PROVISIONING_FAILED, "V2Ray Provisioning Error: " + e.getMessage());
        }
    }
}
