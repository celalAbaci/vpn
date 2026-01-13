package com.celalabaci.payment.google;

import com.celalabaci.dto.subscription.SubscriptionDto;
import com.celalabaci.entity.User;

/**
 * Google Play ödemelerini doğrulamak ve işlemek için
 * kullanılacak servis arayüzü.
 */
public interface IGooglePaymentService {

    /**
     * İstemciden (Android) gelen bir satın alma token'ını doğrular,
     * Google ile konuşur ve başarılıysa yerel aboneliği ve ödemeyi kaydeder.
     *
     * @param receipt     İstemciden gelen purchaseToken ve productId bilgilerini içeren DTO.
     * @param currentUser İsteği yapan, kimliği doğrulanmış kullanıcı.
     * @return Başarıyla oluşturulan veya güncellenen aboneliğin DTO'su.
     */
    SubscriptionDto verifyPurchase(GoogleReceiptDto receipt, User currentUser);
}
