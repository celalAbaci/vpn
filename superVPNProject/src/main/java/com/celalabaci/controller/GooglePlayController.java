package com.celalabaci.controller;

import com.celalabaci.common.ApiResponse;
import com.celalabaci.dto.subscription.SubscriptionDto;
import com.celalabaci.entity.User;
import com.celalabaci.payment.google.GooglePaymentServiceImpl;
import com.celalabaci.payment.google.GoogleReceiptDto;
import com.celalabaci.payment.google.IGooglePaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Google Play Store'dan gelen ödeme ve abonelik
 * doğrulama isteklerini yöneten controller.
 */
@RestController
@RequestMapping("/api/v1/payment/google")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')") // Sadece giriş yapmış kullanıcılar doğrulama yapabilir
@ConditionalOnProperty(name = "google.play.enabled", havingValue = "true")
public class GooglePlayController {

    private final IGooglePaymentService googlePaymentService;

    /**
     * Android istemcisi, Google Play'den bir abonelik satın aldığında,
     * aldığı 'purchaseToken' ve 'productId'yi bu endpoint'e gönderir.
     * Sunucu, bu bilgileri Google ile doğrular ve başarılıysa aboneliği aktive eder.
     *
     * @param receipt     Doğrulama bilgilerini içeren DTO.
     * @param currentUser İsteği yapan kullanıcı.
     * @return Başarılı olursa, yeni oluşturulan abonelik bilgisi.
     */
    @PostMapping("/verify-subscription")
    public ResponseEntity<ApiResponse<SubscriptionDto>> verifySubscriptionPurchase(
            @Valid @RequestBody GoogleReceiptDto receipt,
            @AuthenticationPrincipal User currentUser) {

        // IGooglePaymentService içindeki verifyPurchase metodu tüm
        // doğrulama, abonelik aktivasyonu ve ödeme kaydı mantığını yürütecektir.
        SubscriptionDto newSubscription = googlePaymentService.verifyPurchase(receipt, currentUser);

        return ResponseEntity.ok(ApiResponse.success("Abonelik başarıyla doğrulandı ve aktif edildi.", newSubscription));
    }

    // NOT: Google Play, sunucudan sunucuya bildirimler (Server-to-Server Notifications)
    // için bir Webhook URL'i de ister. Bu, abonelik yenilemeleri, iptalleri vb.
    // durumları yakalamak için kritiktir. Bu, ayrı bir controller
    // (örn: GooglePlayWebhookController) ve güvenliksiz (herkese açık) bir endpoint
    // gerektirir, ancak Google'dan geldiğini doğrulamak için özel bir mantık içerir.
    // Bu implementasyon, şimdilik sadece istemci tarafı doğrulamayı içermektedir.

}
