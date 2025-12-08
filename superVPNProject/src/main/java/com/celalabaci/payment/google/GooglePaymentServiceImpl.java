package com.celalabaci.payment.google;

import com.celalabaci.dto.subscription.SubscriptionDto;
import com.celalabaci.entity.SubscriptionPlan;
import com.celalabaci.entity.Subscription;
import com.celalabaci.entity.User;
import com.celalabaci.exception.BaseException;
import com.celalabaci.exception.MessageType;
import com.celalabaci.exception.PaymentVerificationException;
import com.celalabaci.repository.PaymentRepository;
import com.celalabaci.repository.SubscriptionPlanRepository;
import com.celalabaci.service.IPaymentService;
import com.celalabaci.service.ISubscriptionService;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "google.play.enabled", havingValue = "true")
public class GooglePaymentServiceImpl implements IGooglePaymentService {

    private final AndroidPublisher androidPublisher;
    private final SubscriptionPlanRepository planRepository;
    private final ISubscriptionService subscriptionService;
    private final IPaymentService paymentService;
    private final PaymentRepository paymentRepository; // Mükerrer kontrol için

    @Value("${google.application.package-name}")
    private String packageName;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubscriptionDto verifyPurchase(GoogleReceiptDto receipt, User user) {
        log.info("Google Play doğrulaması başlatıldı. Kullanıcı: {}, Token: {}, ProductID: {}",
                user.getUsername(), receipt.getPurchaseToken(), receipt.getProductId());

        // 1. Mükerrer işlemi en baştan kontrol et
        if (paymentRepository.existsByTransaction(receipt.getPurchaseToken())) {
            throw new PaymentVerificationException(MessageType.PURCHASE_ALREADY_USED, "Bu satın alma token'ı (" + receipt.getPurchaseToken() + ") zaten sistemde kayıtlı.");
        }

        // 2. Google Play Console'daki ürün kimliğine karşılık gelen planı DB'de bul
        // NOT: Bu, SubscriptionPlan entity'nizde 'googleProductId' alanı olduğunu varsayar.
        // Eğer yoksa, 'productId' (örn: "vpn.monthly.plan") ile 'name' alanının eşleştiğini varsayarız.
        SubscriptionPlan plan = planRepository.findByGoogleProductId(receipt.getProductId())
                .orElseThrow(() -> new PaymentVerificationException(MessageType.PLAN_NOT_FOUND_FOR_PRODUCT_ID,
                        "Google Product ID '" + receipt.getProductId() + "' için sistemde bir abonelik planı bulunamadı."));

        // 3. Google API'sini Çağır ve Doğrula
        SubscriptionPurchase purchase;
        try {
            purchase = androidPublisher.purchases().subscriptions()
                    .get(packageName, receipt.getProductId(), receipt.getPurchaseToken())
                    .execute();

            log.debug("Google API'den yanıt alındı: {}", purchase.toPrettyString());

        } catch (IOException e) {
            log.error("Google Play API'ye bağlanırken hata oluştu.", e);
            throw new PaymentVerificationException(MessageType.GOOGLE_PLAY_VERIFICATION_FAILED, "Google Play API ile iletişim kurulamadı: " + e.getMessage());
        }

        // 4. Google Yanıtını Doğrula
        // paymentState: 1 = Ödendi, 0 = Beklemede, 2 = Ücretsiz deneme
        if (purchase.getPaymentState() == null || purchase.getPaymentState() < 0) { // Genellikle 1 (Ödendi) olmalı
            log.warn("Ödeme durumu geçersiz. PaymentState: {}", purchase.getPaymentState());
            //throw new PaymentVerificationException(MessageType.GOOGLE_PLAY_VERIFICATION_FAILED, "Ödeme durumu Google tarafından onaylanmadı (PaymentState: " + purchase.getPaymentState() + ").");
        }

        // expiryTimeMillis: Aboneliğin biteceği zaman
        if (purchase.getExpiryTimeMillis() == null) {
            throw new PaymentVerificationException(MessageType.GOOGLE_PLAY_VERIFICATION_FAILED, "Google yanıtında son kullanma tarihi (expiryTimeMillis) bulunamadı.");
        }

        LocalDate expiryDate = Instant.ofEpochMilli(purchase.getExpiryTimeMillis())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        if (expiryDate.isBefore(LocalDate.now())) {
            throw new PaymentVerificationException(MessageType.GOOGLE_PLAY_VERIFICATION_FAILED, "Bu aboneliğin süresi zaten dolmuş: " + expiryDate);
        }

        // 5. Aboneliği Aktive Et (Mevcut aktif olanı pasif yapar, yenisini açar)
        SubscriptionDto newSubscriptionDto;
        Subscription newSubscriptionEntity;

        try {
            newSubscriptionDto = subscriptionService.activateSubscriptionFromPurchase(
                    user,
                    plan,
                    expiryDate,
                    receipt.getPurchaseToken()
            );

            // Payment kaydı için entity'ye ihtiyacımız var
            newSubscriptionEntity = new Subscription(); // Mapper kullanmadığımız için manuel set ediyoruz
            newSubscriptionEntity.setId(newSubscriptionDto.getId());

        } catch (BaseException e) {
            throw e; // Zaten bizim exception'ımızsa tekrar fırlat
        } catch (Exception e) {
            log.error("Abonelik aktivasyonu sırasında beklenmedik hata.", e);
            throw new PaymentVerificationException(MessageType.GENERAL_EXCEPTION, "Abonelik aktive edilirken sistem hatası: " + e.getMessage());
        }


        // 6. Ödeme Kaydını Oluştur (Mükerrer kontrolü bu metodun içinde tekrar yapılır)
        paymentService.createPaymentRecord(
                user,
                newSubscriptionEntity,
                receipt.getPurchaseToken(),
                plan.getPrice(),
                "COMPLETED"
        );

        log.info("Google Play doğrulaması başarıyla tamamlandı. Abonelik aktif edildi: SubID {}", newSubscriptionDto.getId());

        // 7. İstemciye yeni abonelik bilgisini dön
        return newSubscriptionDto;
    }
}
