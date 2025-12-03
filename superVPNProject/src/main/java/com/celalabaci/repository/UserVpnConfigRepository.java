package com.celalabaci.repository;

import com.celalabaci.dto.config.VpnProtocol;
import com.celalabaci.entity.UserVpnConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserVpnConfigRepository extends JpaRepository<UserVpnConfig, Long> {
    List<UserVpnConfig> findByUserId(Long userId);
    Optional<UserVpnConfig> findByUserIdAndServerIdAndProtocol(Long userId, Long serverId, VpnProtocol protocol);
}