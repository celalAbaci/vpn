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
        if (username.startsWith("GUEST_")) {
            // Guest kullanıcılar veritabanında 'users' tablosunda yok.
            // Onlar için transient bir UserDetails oluşturuyoruz.
            com.celalabaci.entity.User guestUser = new com.celalabaci.entity.User();
            guestUser.setUsername(username);
            guestUser.setRole(com.celalabaci.entity.Role.GUEST);
            guestUser.setEnabled(true);
            guestUser.setPasswordHash(""); // Şifre yok
            return guestUser;
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}