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
        // Special handling for Guest users
        if (username.startsWith("GUEST_")) {
            User guestUser = new User();
            guestUser.setUsername(username);
            guestUser.setRole(Role.GUEST); // Assuming Role.GUEST is added to Role enum
            guestUser.setPasswordHash(""); // No password
            guestUser.setEnabled(true);
            return guestUser;
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
