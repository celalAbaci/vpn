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

    /**
     * Kimliği doğrulanmış kullanıcı için bir VPN konfigürasyon dosyası oluşturur.
     * Kullanıcının aktif bir aboneliği olmalıdır.
     *
     * (NOT: Bu controller, VpnConfigGenerationRequest DTO'su değiştiği için
     * otomatik olarak Multi-Hop'u destekler. İstemcinin JSON'da "exitServerId"
     * göndermesi yeterlidir.)
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<VpnConfigResponse>> generateConfiguration(
            @Valid @RequestBody VpnConfigGenerationRequest request,
            @AuthenticationPrincipal User currentUser) {

        // VpnConfigServiceImpl.generateConfig metodu artık
        // request.getExitServerId() kontrolünü yapmaktadır.
        VpnConfigResponse response = vpnConfigService.generateConfig(request, currentUser);

        return ResponseEntity.ok(ApiResponse.success("Konfigürasyon başarıyla oluşturuldu.", response));
    }
}
