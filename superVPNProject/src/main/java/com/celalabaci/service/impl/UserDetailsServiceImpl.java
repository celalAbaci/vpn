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
        // Handle Guest Users dynamically
        if (username != null && username.startsWith("GUEST_")) {
            com.celalabaci.entity.User guestUser = new com.celalabaci.entity.User();
            guestUser.setUsername(username);
            guestUser.setPasswordHash(""); // No password
            guestUser.setRole(com.celalabaci.entity.Role.USER);
            guestUser.setEnabled(true);
            return guestUser;
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}