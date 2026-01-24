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
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class GuestAuthController {

    private final UserDeviceRepository userDeviceRepository;
    private final JwtService jwtService;

    @PostMapping("/guest-login")
    public ResponseEntity<AuthResponse> guestLogin(@RequestBody GuestLoginRequest request) {
        String uniqueId = request.getUniqueDeviceId();

        // 1. Cihazı bul veya oluştur
        UserDevice device = userDeviceRepository.findByUniqueDeviceId(uniqueId)
                .orElseGet(() -> {
                    UserDevice newDevice = new UserDevice();
                    newDevice.setUniqueDeviceId(uniqueId);
                    newDevice.setDeviceName("Guest Device " + uniqueId.substring(0, Math.min(uniqueId.length(), 6)));
                    newDevice.setLastSeen(OffsetDateTime.now());
                    newDevice.setActive(true);
                    newDevice.setUser(null); // Misafir cihazı, kullanıcı yok
                    return userDeviceRepository.save(newDevice);
                });

        // Update last seen
        device.setLastSeen(OffsetDateTime.now());
        userDeviceRepository.save(device);

        // 2. Geçici User objesi oluştur (Token üretimi için)
        User guestUser = new User();
        guestUser.setUsername("GUEST_" + uniqueId);
        guestUser.setRole(Role.GUEST);

        // 3. Token üret
        String token = jwtService.generateToken(guestUser);

        // Misafirler için şimdilik refresh token yok veya null gönderiyoruz.
        return ResponseEntity.ok(new AuthResponse(token, null));
    }
}
