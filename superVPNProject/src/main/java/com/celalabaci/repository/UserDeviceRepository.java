package com.celalabaci.repository;

import com.celalabaci.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

    /**
     * Finds all devices associated with a specific user ID.
     * @param userId The ID of the user.
     * @return A list of the user's devices.
     */
    List<UserDevice> findByUserId(Long userId);

    /**
     * Checks if a user already has a device with the given name (case-insensitive).
     * @param userId The ID of the user.
     * @param deviceName The name of the device.
     * @return true if a device with this name exists for the user, false otherwise.
     */
    boolean existsByUserIdAndDeviceNameIgnoreCase(Long userId, String deviceName);

    /**
     * Finds a specific device by its ID and the owner's user ID.
     * This is useful for verifying ownership.
     * @param id The ID of the device.
     * @param userId The ID of the user.
     * @return An Optional containing the device if found and owned by the user.
     */
    Optional<UserDevice> findByIdAndUserId(Long id, Long userId);

    /**
     * Finds a device by its unique device ID.
     * @param uniqueDeviceId The unique ID of the device.
     * @return An Optional containing the device if found.
     */
    Optional<UserDevice> findByUniqueDeviceId(String uniqueDeviceId);
}
