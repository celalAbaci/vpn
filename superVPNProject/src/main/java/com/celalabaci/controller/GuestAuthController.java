package com.celalabaci.controller;

import com.celalabaci.dto.AuthResponse;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class GuestAuthController {

    private final UserDeviceRepository userDeviceRepository;
    private final JwtService jwtService;

    @PostMapping("/guest")
    public ResponseEntity<AuthResponse> guestLogin(@RequestBody GuestLoginRequest request) {
        String uniqueId = request.getUniqueDeviceId();

        Optional<UserDevice> deviceOpt = userDeviceRepository.findByUniqueDeviceId(uniqueId);

        UserDevice device;
        if (deviceOpt.isPresent()) {
            device = deviceOpt.get();
        } else {
            device = new UserDevice();
            device.setUniqueDeviceId(uniqueId);
            device.setDeviceName(request.getDeviceName() != null ? request.getDeviceName() : "Guest Device");
            device.setLastSeen(OffsetDateTime.now());
            device.setActive(true);
            userDeviceRepository.save(device);
        }

        // Create transient guest user for Token generation
        User guestUser = new User();
        guestUser.setUsername("GUEST_" + uniqueId);
        guestUser.setRole(Role.GUEST);

        String token = jwtService.generateToken(guestUser);

        return ResponseEntity.ok(new AuthResponse(token, null));
    }
}
