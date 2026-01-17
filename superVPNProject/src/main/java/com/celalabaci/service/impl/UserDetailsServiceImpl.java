package com.celalabaci.service.impl;

import com.celalabaci.entity.Role;
import com.celalabaci.entity.User;
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
        // Handle Guest Users dynamically without DB lookup if they follow the pattern
        if (username != null && username.startsWith("GUEST_")) {
            User guestUser = new User();
            guestUser.setUsername(username);
            guestUser.setPasswordHash(""); // No password
            guestUser.setRole(Role.GUEST);
            guestUser.setEnabled(true);
            return guestUser;
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
