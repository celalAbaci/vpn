package com.celalabaci.repository;

import com.celalabaci.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // YENİ IMPORT
import org.springframework.data.repository.query.Param; // YENİ IMPORT
import org.springframework.stereotype.Repository;

import java.util.Optional; // YENİ IMPORT

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    /**
     * Checks if a subscription plan with the given name already exists (case-insensitive).
     * @param name The name of the plan to check.
     * @return true if a plan with this name exists, false otherwise.
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * YENİ EKLENDİ:
     * Google Play Console'daki ürün kimliğini (productId) kullanarak
     * sistemdeki abonelik planını bulur.
     *
     * NOT: Bu metot, SubscriptionPlan entity'nize bir 'googleProductId' alanı
     * eklemenizi gerektirir. Şimdilik 'name' alanını kullandığınızı varsayıyorum.
     *
     * ÖNERİ: SubscriptionPlan entity'nize aşağıdaki alanı ekleyin:
     * @Column(name = "google_product_id", unique = true, length = 100)
     * private String googleProductId;
     *
     * Şimdilik 'name' alanı üzerinden gidiyoruz (googleProductId = name).
     */
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.name = :productId")
    Optional<SubscriptionPlan> findByGoogleProductId(@Param("productId") String productId);

    // Eğer 'googleProductId' alanını eklerseniz, yukarıdaki @Query'yi şununla değiştirin:
    // Optional<SubscriptionPlan> findByGoogleProductId(String googleProductId);
}
