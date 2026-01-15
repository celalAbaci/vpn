package com.celalabaci.controller;

import com.celalabaci.dto.AuthResponse;
import com.celalabaci.dto.GuestLoginRequest;
import com.celalabaci.entity.Role;
import com.celalabaci.entity.User;
import com.celalabaci.entity.UserDevice;
import com.celalabaci.jwt.JwtService;
import com.celalabaci.repository.UserDeviceRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class GuestAuthController {

    private final UserDeviceRepository userDeviceRepository;
    private final JwtService jwtService;

    @PostMapping("/guest-login")
    public ResponseEntity<AuthResponse> guestLogin(@Valid @RequestBody GuestLoginRequest request) {

        // 1. Check or Create Device
        UserDevice device = userDeviceRepository.findByUniqueDeviceId(request.getUniqueDeviceId())
                .orElseGet(() -> {
                    UserDevice newDevice = new UserDevice();
                    newDevice.setUniqueDeviceId(request.getUniqueDeviceId());
                    newDevice.setDeviceName(request.getDeviceName() != null ? request.getDeviceName() : "Unknown Android Device");
                    newDevice.setUser(null); // Guest
                    newDevice.setActive(true);
                    newDevice.setLastSeen(OffsetDateTime.now());
                    return userDeviceRepository.save(newDevice);
                });

        // Update last seen
        device.setLastSeen(OffsetDateTime.now());
        userDeviceRepository.save(device);

        // 2. Create Transient User for Token Generation
        User guestUser = new User();
        guestUser.setUsername("GUEST_" + request.getUniqueDeviceId());
        guestUser.setRole(Role.USER); // Default role
        // No password, no ID, no DB save for the user object itself

        // 3. Generate Token
        String accessToken = jwtService.generateToken(guestUser);

        // 4. Return Response (No Refresh Token for Guests)
        return ResponseEntity.ok(new AuthResponse(accessToken, null));
    }
}
