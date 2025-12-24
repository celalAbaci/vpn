package com.celalabaci.controller;

import com.celalabaci.common.ApiResponse;
import com.celalabaci.dto.config.VpnConfigGenerationRequest;
import com.celalabaci.dto.config.VpnConfigResponse;
import com.celalabaci.entity.User;
import com.celalabaci.service.IVpnConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class VpnConfigController {

    private final IVpnConfigService vpnConfigService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<VpnConfigResponse>> generateConfiguration(
            @Valid @RequestBody VpnConfigGenerationRequest request,
            @AuthenticationPrincipal User currentUser) {

        // Guest logic: if no user but deviceId is present
        if (currentUser == null && request.getGuestDeviceId() != null) {
             VpnConfigResponse response = vpnConfigService.generateConfig(request, null);
             return ResponseEntity.ok(ApiResponse.success("Misafir konfigürasyon başarıyla oluşturuldu.", response));
        }

        VpnConfigResponse response = vpnConfigService.generateConfig(request, currentUser);

        return ResponseEntity.ok(ApiResponse.success("Konfigürasyon başarıyla oluşturuldu.", response));
    }
}
