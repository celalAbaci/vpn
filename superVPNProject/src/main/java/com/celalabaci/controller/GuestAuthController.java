package com.celalabaci.controller;

import com.celalabaci.dto.AuthResponse;
import com.celalabaci.dto.GuestLoginRequest;
import com.celalabaci.entity.Role;
import com.celalabaci.entity.User;
import com.celalabaci.entity.UserDevice;
import com.celalabaci.jwt.JwtService;
import com.celalabaci.repository.UserDeviceRepository;
import com.celalabaci.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class GuestAuthController {

    private final JwtService jwtService;
    private final UserDeviceRepository userDeviceRepository;
    private final UserRepository userRepository; // Optional if we want to create a shadow user

    @PostMapping("/guest-login")
    public ResponseEntity<AuthResponse> guestLogin(@RequestBody GuestLoginRequest request) {
        String deviceId = request.getDeviceId();

        // 1. Find or Create Device
        Optional<UserDevice> deviceOpt = userDeviceRepository.findByUniqueDeviceId(deviceId);
        UserDevice device;
        if (deviceOpt.isPresent()) {
            device = deviceOpt.get();
        } else {
            device = new UserDevice();
            device.setUniqueDeviceId(deviceId);
            device.setDeviceName("Guest Device - " + deviceId.substring(0, Math.min(deviceId.length(), 8)));
            device.setActive(true);
            device.setLastSeen(OffsetDateTime.now());
            // No user linked for pure guest
            userDeviceRepository.save(device);
        }

        // 2. Create Transient UserDetails for Token Generation
        // We do NOT save a User entity for every guest to keep the users table clean,
        // or we could depending on requirements.
        // Prompt says: "misafirler için user_id null olabilir" in devices table, implying no User record needed.
        // But for Spring Security context, we need a principal.

        UserDetails guestUser = new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + Role.GUEST.name()));
            }

            @Override
            public String getPassword() {
                return "";
            }

            @Override
            public String getUsername() {
                return "GUEST_" + deviceId;
            }

            @Override
            public boolean isAccountNonExpired() { return true; }
            @Override
            public boolean isAccountNonLocked() { return true; }
            @Override
            public boolean isCredentialsNonExpired() { return true; }
            @Override
            public boolean isEnabled() { return true; }
        };

        // 3. Generate Token
        String jwtToken = jwtService.generateToken(guestUser);

        // For guest, refresh token concept might be simpler or same.
        // We'll return the same token or a dummy one if not using refresh flow for guests.
        return ResponseEntity.ok(new AuthResponse(jwtToken, "GUEST_REFRESH_TOKEN"));
    }
}
