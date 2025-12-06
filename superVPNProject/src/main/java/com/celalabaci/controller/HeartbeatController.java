package com.celalabaci.controller;

import com.celalabaci.common.ApiResponse;
import com.celalabaci.dto.user.HeartbeatResponse;
import com.celalabaci.entity.User;
import com.celalabaci.service.IHeartbeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * İstemci (VPN uygulaması) için "Kill Switch" desteği sağlayan
 * "heartbeat" (kalp atışı) endpoint'ini sunar.
 */
@RestController
// Projenizin yapısına uygun olarak /api/v1 prefix'ini kullanıyoruz
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class HeartbeatController {

    private final IHeartbeatService heartbeatService;

    /**
     * İstemcinin oturumunun ve aboneliğinin hala geçerli olup olmadığını
     * kontrol etmesi için periyodik olarak çağıracağı endpoint.
     * Sadece kimliği doğrulanmış (aktif JWT'si olan) USER rolündeki
     * kullanıcılar erişebilir.
     *
     * @param currentUser JWT'den alınan, kimliği doğrulanmış kullanıcı.
     * @return HeartbeatResponse DTO'sunu içeren bir ApiResponse.
     */
    @GetMapping("/heartbeat")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<HeartbeatResponse>> checkHeartbeat(
            @AuthenticationPrincipal User currentUser) {

        // currentUser null ise, Spring Security (AuthEntryPoint) zaten 401 Unauthorized
        // dönecektir, bu metot hiç çağrılmaz.

        HeartbeatResponse response = heartbeatService.checkUserStatus(currentUser);

        // Dönen yanıta göre istemci bağlantıyı açık tutar veya sonlandırır (Kill Switch).
        return ResponseEntity.ok(ApiResponse.success(response.getReason(), response));
    }
}
