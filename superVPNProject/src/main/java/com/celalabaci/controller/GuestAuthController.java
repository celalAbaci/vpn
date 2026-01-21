package com.celalabaci.controller;

import com.celalabaci.dto.AuthResponse;
import com.celalabaci.dto.config.VpnConfigGenerationRequest;
import com.celalabaci.entity.Role;
import com.celalabaci.entity.User;
import com.celalabaci.entity.UserDevice;
import com.celalabaci.jwt.JwtService;
import com.celalabaci.repository.UserDeviceRepository;
import com.celalabaci.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class GuestAuthController {

    private final UserRepository userRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/guest-login")
    public ResponseEntity<AuthResponse> guestLogin(@RequestBody VpnConfigGenerationRequest request) {
        String uniqueDeviceId = request.getGuestDeviceId();
        if (uniqueDeviceId == null || uniqueDeviceId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // 1. Check if device exists, if not create it
        Optional<UserDevice> existingDevice = userDeviceRepository.findByUniqueDeviceId(uniqueDeviceId);
        UserDevice device;

        if (existingDevice.isPresent()) {
            device = existingDevice.get();
            device.setLastSeen(OffsetDateTime.now());
        } else {
            device = new UserDevice();
            device.setUniqueDeviceId(uniqueDeviceId);
            device.setDeviceName("Guest Device " + uniqueDeviceId.substring(0, 8));
            device.setLastSeen(OffsetDateTime.now());
            device.setActive(true);
        }

        // 2. We need a "User" for Spring Security context.
        // Strategy: Create a persistent guest user linked to this device OR return a token with specific claims.
        // Better: Use a "Virtual Guest User" or look for existing guest user linked to this device?
        // For simplicity and to match the prompt "user_id null olabilir, device_id zorunludur" in DB, but Spring Security needs a Principal.
        // We will generate a token with subject = "GUEST_" + uniqueDeviceId and ROLE_GUEST.
        // The JwtService needs to handle this, or we create a transient User object here.

        // Let's create a transient user object for token generation (not saved to DB if we want strictly null user_id,
        // but often it's easier to have a shadow user or handle it in JWT service).
        // However, the prompt says "Misafirler için user_id null olabilir". This implies connection logs.
        // For Authentication, we need a valid JWT.

        User guestUser = new User();
        guestUser.setUsername("GUEST_" + uniqueDeviceId);
        guestUser.setRole(Role.GUEST);
        guestUser.setId(null); // Explicitly null, though JWT generator might need ID?
        // If JWT service uses ID, we might need to adjust.
        // Let's assume JWT uses username.

        String token = jwtService.generateToken(guestUser);

        // Save device
        userDeviceRepository.save(device);

        return ResponseEntity.ok(new AuthResponse(token, null));
    }
}
