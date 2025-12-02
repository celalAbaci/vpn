package com.celalabaci.service;

import com.celalabaci.dto.subscription.AdminSubscriptionUpdateDto;
import com.celalabaci.dto.subscription.SubscriptionCreateDto;
import com.celalabaci.dto.subscription.SubscriptionDto;
import com.celalabaci.entity.SubscriptionPlan; // YENİ IMPORT
import com.celalabaci.entity.User;

import java.time.LocalDate; // YENİ IMPORT
import java.util.List;

public interface ISubscriptionService {

    // User-specific methods
    List<SubscriptionDto> getMySubscriptions(User currentUser);

    // Bu metot artık manuel, ücretsiz veya test abonelikleri için kullanılabilir.
    SubscriptionDto createMySubscription(SubscriptionCreateDto dto, User currentUser);

    SubscriptionDto cancelMySubscription(Long subscriptionId, User currentUser);

    // Admin-specific methods
    List<SubscriptionDto> getAllSubscriptions();

    SubscriptionDto getSubscriptionByIdForAdmin(Long id);

    SubscriptionDto updateSubscriptionByAdmin(Long id, AdminSubscriptionUpdateDto dto);


    /**
     * YENİ EKLENDİ:
     * Google Play gibi harici bir kaynaktan gelen doğrulanmış bir satın alma
     * işlemini sisteme kaydeder.
     *
     * @param user       Aboneliği alan kullanıcı.
     * @param plan       Satın alınan abonelik planı.
     * @param expiryDate Google Play'den gelen son kullanma tarihi.
     * @param transactionId Google Play'den gelen 'purchaseToken'.
     * @return Oluşturulan veya güncellenen aboneliğin DTO'su.
     */
    SubscriptionDto activateSubscriptionFromPurchase(User user, SubscriptionPlan plan, LocalDate expiryDate, String transactionId);
}
