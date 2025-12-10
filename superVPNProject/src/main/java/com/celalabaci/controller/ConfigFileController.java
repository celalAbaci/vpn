package com.celalabaci.controller;

import com.celalabaci.dto.config.CustomDnsProvider;
import com.celalabaci.dto.config.VpnConfigGenerationRequest;
import com.celalabaci.dto.config.VpnConfigResponse;
import com.celalabaci.dto.config.VpnProtocol;
import com.celalabaci.entity.User;
import com.celalabaci.service.IVpnConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Kullanıcının doğrudan konfigürasyon dosyalarını indirmesi için API endpoint'i.
 * GÜNCELLEME: WIREGUARD kaldırıldı. V2RAY ve SUPER eklendi.
 */
@RestController
@RequestMapping("/api/v1/user/servers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class ConfigFileController {

    private final IVpnConfigService vpnConfigService;

    @GetMapping("/{id}/config")
    public ResponseEntity<String> generateConfigFile(
            @PathVariable("id") Long entryServerId,
            @RequestParam(name = "exitServerId", required = false) Long exitServerId,
            @RequestParam("protocol") VpnProtocol protocol,
            @RequestParam("deviceId") Long deviceId,
            @RequestParam(name = "dnsProvider", required = false) CustomDnsProvider dnsProvider,
            @AuthenticationPrincipal User currentUser) {

        // 1. Request DTO oluştur
        VpnConfigGenerationRequest request = new VpnConfigGenerationRequest();
        request.setEntryServerId(entryServerId);
        request.setExitServerId(exitServerId);
        request.setProtocol(protocol);
        request.setDeviceId(deviceId);
        request.setDnsProvider(dnsProvider);

        // 2. Servisi çağır
        VpnConfigResponse configResponse = vpnConfigService.generateConfig(request, currentUser);

        // 3. Dosya adı ve tipi belirle
        String fileName;
        String mediaType = MediaType.TEXT_PLAIN_VALUE;
        String prefix = (exitServerId != null) ? "multi-hop_" : "";
        String serverNameClean = configResponse.getServerName().replaceAll("\\s+", "_");

        switch (protocol) {
            case OPENVPN:
                fileName = prefix + "openvpn_" + serverNameClean + ".ovpn";
                mediaType = "application/x-openvpn-profile";
                break;

            case V2RAY: // YENİ
                // V2Ray genellikle bir txt dosyasında link olarak veya json olarak verilir.
                fileName = prefix + "v2ray_" + serverNameClean + ".txt";
                mediaType = "text/plain";
                break;

            case SUPER: // YENİ
                fileName = prefix + "super_" + serverNameClean + ".txt";
                mediaType = "text/plain";
                break;

            case IKEV2:
            default:
                fileName = prefix + "ikev2_info.txt";
                break;
        }

        // 4. Response dön
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(mediaType))
                .body(configResponse.getConfigurationFileContent());
    }
}