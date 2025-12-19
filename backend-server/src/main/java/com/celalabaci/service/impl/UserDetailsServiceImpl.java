package com.celalabaci.service.impl;

import com.celalabaci.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Handle Guest Users (Prefixed with GUEST_)
        if (username != null && username.startsWith("GUEST_")) {
             // Create a dummy user object for the guest session
             // Password is not needed for JWT auth flow usually, but we set a placeholder.
             com.celalabaci.entity.User guestUser = com.celalabaci.entity.User.builder()
                 .username(username)
                 .password("")
                 .role(com.celalabaci.entity.Role.USER) // Guests treated as basic users
                 .build();
             return guestUser;
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}