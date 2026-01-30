package com.celalabaci.controller;

import com.celalabaci.dto.GuestAuthResponse;
import com.celalabaci.dto.GuestLoginRequest;
import com.celalabaci.entity.Role;
import com.celalabaci.entity.User;
import com.celalabaci.entity.UserDevice;
import com.celalabaci.jwt.JwtService;
import com.celalabaci.repository.UserDeviceRepository;
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
    public ResponseEntity<GuestAuthResponse> guestLogin(@RequestBody GuestLoginRequest request) {
        String uniqueId = request.getUniqueDeviceId();
        if (uniqueId == null || uniqueId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        UserDevice device = userDeviceRepository.findByUniqueDeviceId(uniqueId)
                .orElseGet(() -> {
                    UserDevice newDevice = new UserDevice();
                    newDevice.setUniqueDeviceId(uniqueId);
                    newDevice.setDeviceName(request.getDeviceName() != null ? request.getDeviceName() : "Guest Device");
                    newDevice.setActive(true);
                    newDevice.setLastSeen(OffsetDateTime.now());
                    return userDeviceRepository.save(newDevice);
                });

        // Update last seen
        device.setLastSeen(OffsetDateTime.now());
        userDeviceRepository.save(device);

        // Generate Token
        // Create a transient user for token generation
        User guestUser = new User();
        guestUser.setUsername("GUEST_" + uniqueId);
        guestUser.setRole(Role.GUEST);

        String token = jwtService.generateToken(guestUser);

        return ResponseEntity.ok(new GuestAuthResponse(token, device.getId()));
    }
}
