package com.celalabaci.controller;

import com.celalabaci.dto.request.GuestLoginRequest;
import com.celalabaci.dto.response.GuestLoginResponse;
import com.celalabaci.entity.Role;
import com.celalabaci.entity.User;
import com.celalabaci.entity.UserDevice;
import com.celalabaci.jwt.JwtService;
import com.celalabaci.repository.UserDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class GuestAuthController {

    private final UserDeviceRepository userDeviceRepository;
    private final JwtService jwtService;

    @PostMapping("/guest-login")
    public ResponseEntity<GuestLoginResponse> guestLogin(@RequestBody GuestLoginRequest request) {
        // 1. Check if device exists
        Optional<UserDevice> deviceOpt = userDeviceRepository.findByUniqueDeviceId(request.getUniqueDeviceId());
        UserDevice device;

        if (deviceOpt.isPresent()) {
            device = deviceOpt.get();
            device.setLastSeen(OffsetDateTime.now());
            device.setDeviceName(request.getDeviceName() != null ? request.getDeviceName() : device.getDeviceName());
        } else {
            device = new UserDevice();
            device.setUniqueDeviceId(request.getUniqueDeviceId());
            device.setDeviceName(request.getDeviceName() != null ? request.getDeviceName() : "Unknown Android Device");
            device.setLastSeen(OffsetDateTime.now());
            device.setActive(true);
            // user is null for guests
        }

        userDeviceRepository.save(device);

        // 2. Generate Token
        // Create a transient UserDetails object for the token generation
        User guestUser = new User();
        // Prefix GUEST_ to allow UserDetailsServiceImpl to recognize it
        guestUser.setUsername("GUEST_" + device.getUniqueDeviceId());
        guestUser.setRole(Role.USER); // Guests effectively have User role access for basic connection

        String token = jwtService.generateToken(guestUser);

        return ResponseEntity.ok(GuestLoginResponse.builder()
                .accessToken(token)
                .message("Guest login successful")
                .build());
    }
}
