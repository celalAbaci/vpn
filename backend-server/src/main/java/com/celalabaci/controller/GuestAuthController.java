package com.celalabaci.controller;

import com.celalabaci.entity.Device;
import com.celalabaci.jwt.JwtService;
import com.celalabaci.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class GuestAuthController {

    private final DeviceRepository deviceRepository;
    private final JwtService jwtService;

    @PostMapping("/guest-login")
    public ResponseEntity<String> guestLogin(@RequestParam String deviceId) {
        // Find or Create Device
        Optional<Device> deviceOpt = deviceRepository.findByUniqueDeviceId(deviceId);
        Device device;
        if (deviceOpt.isPresent()) {
            device = deviceOpt.get();
        } else {
            device = Device.builder()
                    .uniqueDeviceId(deviceId)
                    .build();
            deviceRepository.save(device);
        }

        // Generate Token (Assuming JwtService can handle non-user subjects or we create a dummy user context)
        // For simplicity here, we might need to adjust JwtService to accept a subject and extra claims
        // representing a GUEST role.
        String token = jwtService.generateGuestToken(device.getUniqueDeviceId());

        return ResponseEntity.ok(token);
    }
}
