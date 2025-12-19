package com.celalabaci.repository;

import com.celalabaci.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByUniqueDeviceId(String uniqueDeviceId);
}
