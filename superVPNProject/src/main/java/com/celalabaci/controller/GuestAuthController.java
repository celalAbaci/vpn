package com.celalabaci.controller;

import com.celalabaci.dto.AuthResponse;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class GuestAuthController {

    private final UserDeviceRepository userDeviceRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @PostMapping("/guest-login")
    public ResponseEntity<AuthResponse> guestLogin(@RequestBody Map<String, String> request) {
        String uniqueDeviceId = request.get("uniqueDeviceId");
        if (uniqueDeviceId == null || uniqueDeviceId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        UserDevice device = userDeviceRepository.findByUniqueDeviceId(uniqueDeviceId)
                .orElseGet(() -> {
                    UserDevice newDevice = new UserDevice();
                    newDevice.setUniqueDeviceId(uniqueDeviceId);
                    newDevice.setDeviceName("Guest Device");
                    newDevice.setLastSeen(OffsetDateTime.now());
                    newDevice.setActive(true);
                    return userDeviceRepository.save(newDevice);
                });

        // Create or Find Persistent Guest User
        String username = "GUEST_" + uniqueDeviceId;
        User guestUser = userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User u = new User();
                    u.setUsername(username);
                    u.setEmail("guest_" + uniqueDeviceId + "@dataguardvpn.com");
                    u.setPasswordHash("");
                    u.setRole(Role.GUEST);
                    u.setEnabled(true);
                    u.setLastLogin(OffsetDateTime.now());
                    return userRepository.save(u);
                });

        // Link Device to User if not linked
        if (device.getUser() == null) {
            device.setUser(guestUser);
            userDeviceRepository.save(device);
        }

        String token = jwtService.generateToken(guestUser);

        return ResponseEntity.ok(new AuthResponse(token, null));
    }
}
