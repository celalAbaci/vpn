package com.celalabaci.controller;

import com.celalabaci.dto.auth.GuestLoginRequest;
import com.celalabaci.dto.auth.GuestLoginResponse;
import com.celalabaci.entity.Device;
import com.celalabaci.jwt.JwtService;
import com.celalabaci.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class GuestAuthController {

    private final DeviceRepository deviceRepository;
    private final JwtService jwtService;

    @PostMapping("/guest-login")
    public ResponseEntity<GuestLoginResponse> guestLogin(@RequestBody GuestLoginRequest request) {
        // 1. Check if device exists, otherwise create it
        Device device = deviceRepository.findByUniqueDeviceId(request.getDeviceId())
                .orElseGet(() -> {
                    Device newDevice = Device.builder()
                            .uniqueDeviceId(request.getDeviceId())
                            .firstSeen(LocalDateTime.now())
                            .lastSeen(LocalDateTime.now())
                            .isBanned(false)
                            .build();
                    return deviceRepository.save(newDevice);
                });

        if (device.isBanned()) {
             return ResponseEntity.status(403).build();
        }

        // 2. Generate Token
        // Ensure token is generated with correct claims for Guest Role
        String token = jwtService.generateGuestToken(device.getUniqueDeviceId());

        // 3. Return Response
        return ResponseEntity.ok(new GuestLoginResponse(token, device.getUniqueDeviceId()));
    }
}
