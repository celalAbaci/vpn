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

        // 1. Cihazı bul veya oluştur
        UserDevice device = userDeviceRepository.findByUniqueDeviceId(request.getUniqueDeviceId())
                .orElseGet(() -> {
                    UserDevice newDevice = new UserDevice();
                    newDevice.setUniqueDeviceId(request.getUniqueDeviceId());
                    newDevice.setDeviceName("Guest Device");
                    newDevice.setLastSeen(OffsetDateTime.now());
                    newDevice.setActive(true);
                    newDevice.setUser(null); // Misafir cihazı
                    return userDeviceRepository.save(newDevice);
                });

        // Last seen güncelle
        device.setLastSeen(OffsetDateTime.now());
        userDeviceRepository.save(device);

        // 2. Geçici bir User objesi oluştur (Token üretimi için)
        // DB'ye kaydedilmez, sadece UserDetails arayüzünü sağlar.
        User guestUser = new User();
        guestUser.setUsername("GUEST_" + request.getUniqueDeviceId());
        guestUser.setRole(Role.GUEST);
        guestUser.setEnabled(true);

        // 3. Token üret
        String jwtToken = jwtService.generateToken(guestUser);

        // Misafirler için refresh token şimdilik access token ile aynı veya null olabilir
        // Basitlik adına access token dönüyoruz.
        return ResponseEntity.ok(new AuthResponse(jwtToken, jwtToken));
    }
}
