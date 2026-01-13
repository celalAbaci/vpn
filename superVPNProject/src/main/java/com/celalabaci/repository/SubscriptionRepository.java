package com.celalabaci.repository;

import com.celalabaci.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate; // YENİ IMPORT
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    /**
     * Finds all subscriptions for a given user ID.
     * @param userId The ID of the user.
     * @return A list of subscriptions.
     */
    List<Subscription> findByUserId(Long userId);

    /**
     * Finds the currently active subscription for a given user ID.
     * @param userId The ID of the user.
     * @param isActive The active status (should be true).
     * @return An Optional containing the active subscription if found.
     */
    Optional<Subscription> findByUserIdAndIsActive(Long userId, boolean isActive);

    // YENİ EKLENDİ: Tüm aktif aboneliklerin sayısı
    long countByIsActive(boolean isActive);

    // YENİ EKLENDİ: Zamanlanmış görev için süresi dolmuş ve hala aktif olan abonelikleri bulur.
    /**
     * Finds all subscriptions that are still active but their end date is before the specified date.
     * @param now The date to compare against (e.g., today).
     * @return A list of expired, active subscriptions.
     */
    List<Subscription> findByEndDateBeforeAndIsActiveTrue(LocalDate now);
}
