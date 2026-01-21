package com.celalabaci.repository;

import com.celalabaci.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

    List<UserDevice> findByUserId(Long userId);

    boolean existsByUserIdAndDeviceNameIgnoreCase(Long userId, String deviceName);

    Optional<UserDevice> findByIdAndUserId(Long id, Long userId);

    Optional<UserDevice> findByUniqueDeviceId(String uniqueDeviceId);
}
