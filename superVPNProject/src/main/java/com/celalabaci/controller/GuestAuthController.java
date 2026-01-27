package com.celalabaci.controller;

import com.celalabaci.dto.AuthResponse;
import com.celalabaci.entity.Role;
import com.celalabaci.entity.User;
import com.celalabaci.entity.UserDevice;
import com.celalabaci.jwt.JwtService;
import com.celalabaci.service.IUserDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class GuestAuthController {

    private final IUserDeviceService userDeviceService;
    private final JwtService jwtService;

    @PostMapping("/guest-login")
    public ResponseEntity<AuthResponse> guestLogin(@RequestBody Map<String, String> request) {
        String uniqueDeviceId = request.get("uniqueDeviceId");
        if (uniqueDeviceId == null || uniqueDeviceId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Ensure device exists
        userDeviceService.findOrCreateGuestDevice(uniqueDeviceId);

        // Create transient user for JWT
        User guestUser = new User();
        guestUser.setUsername("GUEST_" + uniqueDeviceId);
        guestUser.setRole(Role.GUEST);
        guestUser.setEnabled(true);
        guestUser.setPasswordHash("");

        String jwt = jwtService.generateToken(guestUser);

        return ResponseEntity.ok(new AuthResponse(jwt, "GUEST_REFRESH_NOT_SUPPORTED"));
    }
}
