package com.celalabaci.controller;

import com.celalabaci.entity.Role;
import com.celalabaci.entity.User;
import com.celalabaci.entity.UserDevice;
import com.celalabaci.jwt.JwtService;
import com.celalabaci.repository.UserDeviceRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class GuestAuthController {

    private final UserDeviceRepository userDeviceRepository;
    private final JwtService jwtService;

    @PostMapping("/guest-login")
    public ResponseEntity<GuestLoginResponse> guestLogin(@RequestBody GuestLoginRequest request) {
        if (request.getUniqueDeviceId() == null || request.getUniqueDeviceId().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Optional<UserDevice> deviceOpt = userDeviceRepository.findByUniqueDeviceId(request.getUniqueDeviceId());
        UserDevice device;

        if (deviceOpt.isPresent()) {
            device = deviceOpt.get();
        } else {
            device = new UserDevice();
            device.setUniqueDeviceId(request.getUniqueDeviceId());
            // Use a substring for name or a default
            String deviceName = "GUEST-" + (request.getUniqueDeviceId().length() > 8 ? request.getUniqueDeviceId().substring(0, 8) : request.getUniqueDeviceId());
            device.setDeviceName(deviceName);
            device.setLastSeen(OffsetDateTime.now());
            device.setActive(true);
            device = userDeviceRepository.save(device);
        }

        // Create a transient User object for Token generation (not saved to DB)
        User guestUser = new User();
        guestUser.setUsername("GUEST_" + device.getUniqueDeviceId());
        guestUser.setRole(Role.GUEST);
        guestUser.setEnabled(true);
        // Password hash not needed for JWT generation usually if we pass UserDetails directly,
        // but if JwtService uses it, we might need a dummy. JwtService only uses username and authorities.

        String token = jwtService.generateToken(guestUser);

        return ResponseEntity.ok(new GuestLoginResponse(token, device.getId()));
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class GuestLoginRequest {
        private String uniqueDeviceId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class GuestLoginResponse {
        private String accessToken;
        private Long deviceId;

        public GuestLoginResponse(String accessToken, Long deviceId) {
            this.accessToken = accessToken;
            this.deviceId = deviceId;
        }
    }
}
