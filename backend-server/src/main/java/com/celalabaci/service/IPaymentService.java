package com.celalabaci.service;

import com.celalabaci.dto.payment.PaymentDto;
import com.celalabaci.entity.Payment; // YENİ IMPORT
import com.celalabaci.entity.Subscription; // YENİ IMPORT
import com.celalabaci.entity.User;

import java.math.BigDecimal; // YENİ IMPORT
import java.util.List;

public interface IPaymentService {

    // User method
    List<PaymentDto> getMyPayments(User currentUser);

    // Admin methods
    List<PaymentDto> getAllPayments();

    List<PaymentDto> getPaymentsByUserId(Long userId);

    PaymentDto getPaymentById(Long id);

    /**
     * YENİ EKLENDİ:
     * Sistem tarafından (örn: Google Play doğrulaması sonrası) yeni bir
     * ödeme kaydı oluşturmak için kullanılır.
     *
     * @param user          Ödemeyi yapan kullanıcı.
     * @param subscription  İlişkili abonelik.
     * @param transactionId Google'dan gelen purchaseToken.
     * @param amount        Abonelik planının ücreti.
     * @param status        Ödeme durumu (örn: "COMPLETED", "PENDING").
     * @return Oluşturulan Payment nesnesi (DTO değil, entity).
     */
    Payment createPaymentRecord(User user, Subscription subscription, String transactionId, BigDecimal amount, String status);
}
