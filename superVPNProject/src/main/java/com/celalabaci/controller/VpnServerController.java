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
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'PREMIUM')")
    public ResponseEntity<ApiResponse<List<VpnServerDto>>> getActiveServers() {
        List<VpnServerDto> servers = vpnServerService.getActiveServersForUsers();
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
