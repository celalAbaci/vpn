package com.celalabaci.repository;

import com.celalabaci.entity.UserConnectionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // YENİ IMPORT
import org.springframework.stereotype.Repository;

import java.math.BigDecimal; // YENİ IMPORT
import java.util.List;

@Repository
public interface UserConnectionLogRepository extends JpaRepository<UserConnectionLog, Long> {

    /**
     * Finds all connection logs for a specific user, ordered by connection time descending.
     * @param userId The ID of the user.
     * @return A list of connection logs.
     */
    List<UserConnectionLog> findByUserIdOrderByConnectTimeDesc(Long userId);

    // YENİ EKLENDİ: Kullanılan toplam datayı hesaplar
    @Query("SELECT SUM(log.dataUsedMb) FROM UserConnectionLog log")
    BigDecimal findTotalDataUsedMb();

    @Query("SELECT SUM(l.dataUsedMb) FROM UserConnectionLog l WHERE l.device.id = :deviceId")
    BigDecimal sumDataUsedMbByDeviceId(Long deviceId);
}
