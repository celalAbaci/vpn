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
        if (username != null && username.startsWith("GUEST_")) {
            User guestUser = new User();
            guestUser.setUsername(username);
            guestUser.setRole(Role.GUEST);
            guestUser.setEnabled(true);
            guestUser.setPasswordHash("");
            // Email is required by Entity but meaningless for transient object.
            // If JPA validation triggers, we might have issues if we tried to save,
            // but here we just return it as UserDetails.
            guestUser.setEmail(username + "@guest.local");
            return guestUser;
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}