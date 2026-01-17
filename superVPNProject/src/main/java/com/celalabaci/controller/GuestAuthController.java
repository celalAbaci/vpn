package com.celalabaci.controller;

import com.celalabaci.dto.*;
import com.celalabaci.service.IAuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class GuestAuthController {

    private final IAuthenticationService authenticationService;

    @PostMapping("/guest-login")
    public ResponseEntity<AuthResponse> guestLogin(@Valid @RequestBody GuestLoginRequest request) {
        return ResponseEntity.ok(authenticationService.guestLogin(request));
    }
}
