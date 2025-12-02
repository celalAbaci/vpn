package com.celalabaci.repository;

import com.celalabaci.entity.UserVpnConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserVpnConfigRepository extends JpaRepository<UserVpnConfig, Long> {
    List<UserVpnConfig> findByUserId(Long userId);
}