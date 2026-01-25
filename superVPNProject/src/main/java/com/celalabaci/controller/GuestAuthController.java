package com.celalabaci.controller;

import com.celalabaci.dto.AuthResponse;
import com.celalabaci.dto.GuestLoginRequest;
import com.celalabaci.entity.Role;
import com.celalabaci.entity.User;
import com.celalabaci.entity.UserDevice;
import com.celalabaci.jwt.JwtService;
import com.celalabaci.repository.UserDeviceRepository;
import com.celalabaci.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @PostMapping("/guest-login")
    public ResponseEntity<AuthResponse> guestLogin(@RequestBody GuestLoginRequest request) {
        String deviceId = request.getUniqueDeviceId();

        // Find or create device
        UserDevice device = userDeviceRepository.findByUniqueDeviceId(deviceId)
                .orElseGet(() -> {
                    UserDevice newDevice = new UserDevice();
                    newDevice.setUniqueDeviceId(deviceId);
                    newDevice.setDeviceName("Guest Device " + (deviceId.length() > 8 ? deviceId.substring(0, 8) : deviceId));
                    newDevice.setLastSeen(OffsetDateTime.now());
                    newDevice.setActive(true);
                    return userDeviceRepository.save(newDevice);
                });

        // Find or create guest user in DB to ensure UserDetailsService can load it
        String username = "GUEST_" + deviceId;
        User guestUser = userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User u = new User();
                    u.setUsername(username);
                    u.setEmail(username + "@guest.local");
                    u.setPasswordHash(java.util.UUID.randomUUID().toString());
                    u.setRole(Role.GUEST);
                    u.setEnabled(true);
                    u.setLastLogin(OffsetDateTime.now());
                    return userRepository.save(u);
                });

        String accessToken = jwtService.generateToken(guestUser);
        String refreshToken = jwtService.generateToken(guestUser);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }
}
