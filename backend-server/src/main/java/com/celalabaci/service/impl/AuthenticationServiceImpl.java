package com.celalabaci.service.impl;

import com.celalabaci.dto.*;
import com.celalabaci.entity.RefreshToken;
import com.celalabaci.entity.Role;
import com.celalabaci.entity.User;
import com.celalabaci.exception.BaseException;
import com.celalabaci.exception.MessageType;
import com.celalabaci.jwt.JwtService;
import com.celalabaci.repository.RefreshTokenRepository;
import com.celalabaci.repository.UserRepository;
import com.celalabaci.service.IAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements IAuthenticationService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public UserDto register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BaseException(MessageType.GENERAL_EXCEPTION, "Username is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BaseException(MessageType.GENERAL_EXCEPTION, "Email is already in use!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER); // Varsayılan rol
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(savedUser, userDto);
        return userDto;
    }

    @Override
    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BaseException(MessageType.USERNAME_NOT_FOUND, request.getUsername()));

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = createAndSaveRefreshToken(user);

        return new AuthResponse(jwtToken, refreshToken.getToken());
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BaseException(MessageType.REFRESH_TOKEN_NOT_FOUND, request.getRefreshToken()));

        if (refreshToken.isRevoked() || refreshToken.isExpired()) {
            throw new BaseException(MessageType.REFRESH_TOKEN_IS_EXPIRED, "Refresh token was expired or revoked.");
        }

        User user = refreshToken.getUser();
        String accessToken = jwtService.generateToken(user);

        // Opsiyonel: Refresh token rotasyonu. Güvenliği artırır.
        // Eski token'ı geçersiz kıl ve yeni bir tane oluştur.
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        RefreshToken newRefreshToken = createAndSaveRefreshToken(user);

        return new AuthResponse(accessToken, newRefreshToken.getToken());
    }

    private RefreshToken createAndSaveRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setRevoked(false);
        // Refresh token ömrünü 7 gün olarak ayarlayalım
        refreshToken.setExpiresAt(OffsetDateTime.now().plusDays(7));
        return refreshTokenRepository.save(refreshToken);
    }
}