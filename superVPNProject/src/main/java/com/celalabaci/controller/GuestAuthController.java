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

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class GuestAuthController {

    private final UserDeviceRepository userDeviceRepository;
    private final JwtService jwtService;

    @PostMapping("/guest-login")
    public ResponseEntity<AuthResponse> guestLogin(@RequestBody GuestLoginRequest request) {
        // Cihazı bul veya oluştur
        UserDevice device = userDeviceRepository.findByUniqueDeviceId(request.getUniqueDeviceId())
                .orElseGet(() -> {
                    UserDevice newDevice = new UserDevice();
                    newDevice.setUniqueDeviceId(request.getUniqueDeviceId());
                    newDevice.setDeviceName(request.getDeviceName() != null ? request.getDeviceName() : "Unknown Guest Device");
                    newDevice.setActive(true);
                    newDevice.setLastSeen(OffsetDateTime.now());
                    newDevice.setUser(null); // Misafir olduğu için user null
                    return userDeviceRepository.save(newDevice);
                });

        // Last seen güncelle
        device.setLastSeen(OffsetDateTime.now());
        userDeviceRepository.save(device);

        // Transient User objesi oluştur (Token üretimi için)
        User guestUser = new User();
        guestUser.setUsername("GUEST_" + request.getUniqueDeviceId());
        guestUser.setRole(Role.GUEST);
        guestUser.setEnabled(true);

        // Token üret
        String jwtToken = jwtService.generateToken(guestUser);

        // Refresh token şu an için guest modunda opsiyonel, null dönebiliriz veya üretebiliriz.
        // Basitlik için sadece access token dönüyoruz.
        return ResponseEntity.ok(new AuthResponse(jwtToken, null));
    }
}
