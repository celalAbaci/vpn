package com.celalabaci.repository;

import com.celalabaci.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime; // YENİ IMPORT
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    // YENİ EKLENDİ: Belirli bir tarihten sonra kaydolan kullanıcıların sayısı
    long countByCreatedAtAfter(OffsetDateTime dateTime);
}
