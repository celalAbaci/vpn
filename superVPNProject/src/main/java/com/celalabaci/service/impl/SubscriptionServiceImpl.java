package com.celalabaci.service.impl;

import com.celalabaci.dto.subscription.AdminSubscriptionUpdateDto;
import com.celalabaci.dto.subscription.SubscriptionCreateDto;
import com.celalabaci.dto.subscription.SubscriptionDto;
import com.celalabaci.entity.Subscription;
import com.celalabaci.entity.SubscriptionPlan;
import com.celalabaci.entity.User;
import com.celalabaci.exception.BaseException;
import com.celalabaci.exception.MessageType;
import com.celalabaci.mapper.SubscriptionMapper;
import com.celalabaci.repository.SubscriptionPlanRepository;
import com.celalabaci.repository.SubscriptionRepository;
import com.celalabaci.service.ISubscriptionService;
import lombok.RequiredArgsConstructor; // YENİ IMPORT
import lombok.extern.slf4j.Slf4j; // YENİ IMPORT
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j // YENİ EKLENDİ
@Service
@RequiredArgsConstructor // @Autowired yerine constructor injection için
public class SubscriptionServiceImpl implements ISubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionDto> getMySubscriptions(User currentUser) {
        return subscriptionRepository.findByUserId(currentUser.getId()).stream()
                .map(subscriptionMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Bu metot artık manuel, ücretsiz veya test abonelikleri için kullanılır.
     * Gerçek ödemeler 'activateSubscriptionFromPurchase' üzerinden gelmelidir.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubscriptionDto createMySubscription(SubscriptionCreateDto dto, User currentUser) {
        // Check if the user already has an active subscription
        subscriptionRepository.findByUserIdAndIsActive(currentUser.getId(), true)
                .ifPresent(s -> {
                    throw new BaseException(MessageType.GENERAL_EXCEPTION, "User already has an active subscription.");
                });

        SubscriptionPlan plan = planRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new BaseException(MessageType.NO_RECORD_EXIST, "Subscription Plan with id " + dto.getPlanId() + " not found."));

        Subscription newSubscription = new Subscription();
        newSubscription.setUser(currentUser);
        newSubscription.setPlan(plan);
        newSubscription.setStartDate(LocalDate.now());
        newSubscription.setEndDate(LocalDate.now().plusDays(plan.getDurationDays()));
        newSubscription.setSpeedLimitMbps(plan.getSpeedLimitMbps());
        newSubscription.setActive(true);

        Subscription savedSubscription = subscriptionRepository.save(newSubscription);
        return subscriptionMapper.toDto(savedSubscription);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubscriptionDto cancelMySubscription(Long subscriptionId, User currentUser) {
        Subscription subscription = findSubscriptionById(subscriptionId);

        // Check if the subscription belongs to the current user
        if (!subscription.getUser().getId().equals(currentUser.getId())) {
            throw new BaseException(MessageType.GENERAL_EXCEPTION, "You are not authorized to cancel this subscription.");
        }

        if (!subscription.isActive()) {
            throw new BaseException(MessageType.GENERAL_EXCEPTION, "This subscription is already inactive.");
        }

        subscription.setActive(false);
        // ÖNEMLİ: Google Play aboneliği ise, iptal buradan yapılmamalıdır.
        // Google Play'den iptal edilmeli ve webhook ile burası güncellenmelidir.
        // Bu metot, sadece manuel oluşturulan aboneliklerin iptali için kullanılmalıdır.
        log.warn("Kullanıcı manuel abonelik iptali yapıyor: UserID {}, SubID {}", currentUser.getId(), subscriptionId);

        // Optionally, you could set the end date to now:
        // subscription.setEndDate(LocalDate.now());
        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        return subscriptionMapper.toDto(updatedSubscription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionDto> getAllSubscriptions() {
        return subscriptionRepository.findAll().stream()
                .map(subscriptionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionDto getSubscriptionByIdForAdmin(Long id) {
        return subscriptionMapper.toDto(findSubscriptionById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubscriptionDto updateSubscriptionByAdmin(Long id, AdminSubscriptionUpdateDto dto) {
        Subscription subscription = findSubscriptionById(id);
        subscription.setActive(dto.getIsActive());
        subscription.setEndDate(dto.getEndDate());
        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        return subscriptionMapper.toDto(updatedSubscription);
    }

    /**
     * YENİ EKLENDİ: ISUubscriptionService arayüzünden gelen metot implementasyonu.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubscriptionDto activateSubscriptionFromPurchase(User user, SubscriptionPlan plan, LocalDate expiryDate, String transactionId) {

        log.info("Kullanıcı için abonelik aktivasyonu başlatılıyor: UserID {}, PlanID {}, Bitiş: {}", user.getId(), plan.getId(), expiryDate);

        // 1. Kullanıcının mevcut diğer AKTİF aboneliklerini bul ve pasif yap
        subscriptionRepository.findByUserIdAndIsActive(user.getId(), true)
                .ifPresent(existingActiveSub -> {
                    log.warn("Mevcut aktif abonelik pasif hale getiriliyor: SubID {}", existingActiveSub.getId());
                    existingActiveSub.setActive(false);
                    subscriptionRepository.save(existingActiveSub);
                });

        // 2. Yeni abonelik kaydını oluştur
        Subscription newSubscription = new Subscription();
        newSubscription.setUser(user);
        newSubscription.setPlan(plan);
        newSubscription.setStartDate(LocalDate.now());
        newSubscription.setEndDate(expiryDate);
        newSubscription.setSpeedLimitMbps(plan.getSpeedLimitMbps());
        newSubscription.setActive(true);
        // NOT: İleride bu 'Subscription' entity'sine 'purchaseToken' gibi bir alan ekleyip
        // transactionId'yi buraya da kaydetmek, yenileme takibi için faydalı olabilir.

        Subscription savedSubscription = subscriptionRepository.save(newSubscription);
        log.info("Yeni abonelik başarıyla oluşturuldu: SubID {}", savedSubscription.getId());

        return subscriptionMapper.toDto(savedSubscription);
    }


    private Subscription findSubscriptionById(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new BaseException(MessageType.NO_RECORD_EXIST, "Subscription with id " + id + " not found."));
    }
}
