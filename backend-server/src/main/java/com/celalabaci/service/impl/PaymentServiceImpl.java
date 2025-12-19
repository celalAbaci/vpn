package com.celalabaci.service.impl;

import com.celalabaci.dto.payment.PaymentDto;
import com.celalabaci.entity.Payment;
import com.celalabaci.entity.Subscription; // YENİ IMPORT
import com.celalabaci.entity.User;
import com.celalabaci.exception.BaseException;
import com.celalabaci.exception.MessageType;
import com.celalabaci.mapper.PaymentMapper;
import com.celalabaci.repository.PaymentRepository;
import com.celalabaci.service.IPaymentService;
import lombok.RequiredArgsConstructor; // YENİ IMPORT
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // YENİ IMPORT

import java.math.BigDecimal; // YENİ IMPORT
import java.time.OffsetDateTime; // YENİ IMPORT
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // @Autowired yerine constructor injection için
public class PaymentServiceImpl implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional(readOnly = true) // Okuma işlemi olduğu için
    public List<PaymentDto> getMyPayments(User currentUser) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId()).stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDto> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDto> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDto getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new BaseException(MessageType.NO_RECORD_EXIST, "Payment with id " + id + " not found."));
        return paymentMapper.toDto(payment);
    }

    /**
     * YENİ EKLENDİ: IPaymentService arayüzünden gelen metot implementasyonu.
     * Bu metot, Google Play'den gelen doğrulanmış bir işlemi veritabanına kaydeder.
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // Hata olursa işlemi geri al
    public Payment createPaymentRecord(User user, Subscription subscription, String transactionId, BigDecimal amount, String status) {

        // Mükerrer işlemi engelle (Çok ÖNEMLİ)
        if (paymentRepository.existsByTransaction(transactionId)) {
            throw new BaseException(MessageType.PURCHASE_ALREADY_USED, "Transaction ID: " + transactionId);
        }

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setSubscription(subscription);
        payment.setAmount(amount);
        payment.setTransaction(transactionId); // Google'ın purchaseToken'ı
        payment.setStatus(status); // "COMPLETED"
        payment.setPaymentDate(OffsetDateTime.now()); // Ödemenin doğrulandığı an

        try {
            return paymentRepository.save(payment);
        } catch (Exception e) {
            // Veritabanı hatası veya unique constraint ihlali
            throw new BaseException(MessageType.PAYMENT_TRANSACTION_FAILED, "Payment kaydı oluşturulamadı: " + e.getMessage());
        }
    }
}
