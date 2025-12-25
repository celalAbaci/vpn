package com.celalabaci.controller;

import com.celalabaci.common.ApiResponse;
import com.celalabaci.dto.vpnserver.VpnServerDto;
import com.celalabaci.dto.vpnserver.VpnServerCreateUpdateDto;
import com.celalabaci.service.IVpnServerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/servers")
public class VpnServerController {

    @Autowired
    private IVpnServerService vpnServerService;

    // Kullanıcıların ve adminlerin erişebileceği, sadece aktif sunucuları listeleyen endpoint
    // Guest (AuthenticationPrincipal is null or anonymous) is handled by SecurityConfig if permitAll.
    // However, if we want tiered access based on roles here:
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'PREMIUM')")
    public ResponseEntity<ApiResponse<List<VpnServerDto>>> getActiveServers(java.security.Principal principal) {
        List<VpnServerDto> servers = vpnServerService.getActiveServersForUsers();

        // Filter for Guest/Free Users
        // Free user = Role is USER (not PREMIUM, not ADMIN) OR username starts with GUEST_
        boolean isFreeUser = false;
        if (principal != null) {
            String username = principal.getName();
            if (username.startsWith("GUEST_")) {
                isFreeUser = true;
            } else {
                 // Check if user has ROLE_PREMIUM. If not, they are Free.
                 // Note: Ideally we check role from Authentication object, but here we only have Principal.
                 // Assuming standard users without premium subscription are 'Free'.
                 // We can cast Principal to Authentication but let's be safe.
                 org.springframework.security.core.Authentication auth = (org.springframework.security.core.Authentication) principal;
                 boolean isPremium = auth.getAuthorities().stream()
                         .anyMatch(a -> a.getAuthority().equals("ROLE_PREMIUM") || a.getAuthority().equals("ROLE_ADMIN"));

                 if (!isPremium) {
                     isFreeUser = true;
                 }
            }
        }

        if (isFreeUser) {
             List<VpnServerDto> freeServers = servers.stream()
                 .filter(VpnServerDto::isFree) // Assuming isFree is added to DTO or mapped
                 .limit(5)
                 .toList();

             // Fallback if no "Free" servers marked
             if (freeServers.isEmpty()) {
                 freeServers = servers.stream().limit(5).toList();
             }
             return ResponseEntity.ok(ApiResponse.success(freeServers));
        }

        return ResponseEntity.ok(ApiResponse.success(servers));
    }

    // Sadece adminlerin erişebileceği, tüm sunucuları (aktif/pasif) listeleyen endpoint
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<VpnServerDto>>> getAllServers() {
        List<VpnServerDto> servers = vpnServerService.getAllServersForAdmin();
        return ResponseEntity.ok(ApiResponse.success(servers));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'PREMIUM')")
    public ResponseEntity<ApiResponse<VpnServerDto>> getServerById(@PathVariable Long id) {
        VpnServerDto server = vpnServerService.getServerById(id);
        return ResponseEntity.ok(ApiResponse.success(server));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VpnServerDto>> createServer(@Valid @RequestBody VpnServerCreateUpdateDto dto) {
        VpnServerDto createdServer = vpnServerService.createServer(dto);
        return new ResponseEntity<>(ApiResponse.success("VPN Server created successfully.", createdServer), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VpnServerDto>> updateServer(@PathVariable Long id, @Valid @RequestBody VpnServerCreateUpdateDto dto) {
        VpnServerDto updatedServer = vpnServerService.updateServer(id, dto);
        return ResponseEntity.ok(ApiResponse.success("VPN Server updated successfully.", updatedServer));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteServer(@PathVariable Long id) {
        vpnServerService.deleteServer(id);
        return ResponseEntity.ok(ApiResponse.success("VPN Server deleted successfully.", null));
    }
}
