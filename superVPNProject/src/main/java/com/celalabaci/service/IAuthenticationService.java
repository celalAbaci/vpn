package com.celalabaci.service;

import com.celalabaci.dto.*;

public interface IAuthenticationService {
    UserDto register(RegisterRequest request);
    AuthResponse authenticate(AuthRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    AuthResponse guestLogin(GuestLoginRequest request); // Added
}
