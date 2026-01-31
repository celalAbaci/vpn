package com.celalabaci.controller;

import com.celalabaci.entity.Role;
import com.celalabaci.entity.User;
import com.celalabaci.entity.UserDevice;
import com.celalabaci.jwt.JwtService;
import com.celalabaci.repository.UserDeviceRepository;
import com.celalabaci.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class GuestAuthController {

    private final UserDeviceRepository userDeviceRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/guest")
    public ResponseEntity<?> guestLogin(@RequestBody Map<String, String> request) {
        String uniqueDeviceId = request.get("uniqueDeviceId");
        if (uniqueDeviceId == null || uniqueDeviceId.isEmpty()) {
            return ResponseEntity.badRequest().body("Device ID is required");
        }

        // 1. Find or Create Device
        Optional<UserDevice> deviceOpt = userDeviceRepository.findByUniqueDeviceId(uniqueDeviceId);
        UserDevice device;
        if (deviceOpt.isPresent()) {
            device = deviceOpt.get();
        } else {
            device = new UserDevice();
            device.setUniqueDeviceId(uniqueDeviceId);
            device.setDeviceName("Guest Device");
            device.setActive(true);
            userDeviceRepository.save(device);
        }

        // 2. Find or Create Guest User
        String username = "GUEST_" + uniqueDeviceId;
        Optional<User> userOpt = userRepository.findByUsername(username);
        User user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Random password
            user.setRole(Role.GUEST);
            user.setEmail(uniqueDeviceId + "@guest.local"); // Dummy email
            userRepository.save(user);
        }

        // 3. Generate Token
        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(Map.of("token", token, "role", "GUEST", "deviceId", device.getId()));
    }
}
