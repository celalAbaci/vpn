package com.celalabaci.repository;

import com.celalabaci.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional; // YENİ IMPORT

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED'") // Sadece tamamlananları topla
    BigDecimal findTotalRevenue();

    /**
     * YENİ EKLENDİ:
     * Belirli bir işlem kimliğine (Google'dan gelen purchaseToken) sahip
     * bir ödemenin zaten var olup olmadığını kontrol eder.
     * Bu, mükerrer işlemleri engellemek için kritik öneme sahiptir.
     *
     * @param transactionId Google Play'in 'purchaseToken'ı.
     * @return true, eğer bu transactionId ile bir kayıt varsa.
     */
    boolean existsByTransaction(String transactionId);
}
