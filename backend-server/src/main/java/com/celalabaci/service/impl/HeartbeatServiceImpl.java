package com.celalabaci.service.impl;

import com.celalabaci.dto.user.HeartbeatResponse;
import com.celalabaci.entity.User;
import com.celalabaci.repository.SubscriptionRepository;
import com.celalabaci.service.IHeartbeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * IHeartbeatService arayüzünün implementasyonu.
 * Kullanıcının abonelik ve hesap durumunu kontrol eder.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HeartbeatServiceImpl implements IHeartbeatService {

    private final SubscriptionRepository subscriptionRepository;

    // Bu kontrol çok sık yapılacağı için veritabanına sadece okuma amaçlı
    // erişeceğini belirtiyoruz (readOnly = true).
    @Override
    @Transactional(readOnly = true)
    public HeartbeatResponse checkUserStatus(User currentUser) {

        // 1. Spring Security'den gelen UserDetails kontrolü
        // (Eğer hesap kilitli veya devre dışı ise Spring Security zaten 401/403 döner,
        // ancak bu yine de ekstra bir güvenlik katmanıdır.)
        if (!currentUser.isEnabled() || !currentUser.isAccountNonLocked()) {
            log.warn("Heartbeat başarısız: Kullanıcı hesabı kilitli veya devre dışı. Kullanıcı: {}", currentUser.getUsername());
            return new HeartbeatResponse(false, "ACCOUNT_DISABLED");
        }

        // 2. Aktif Abonelik Kontrolü
        // Projenin exception mimarisine uygun olarak, burada "kayıt bulunamadı"
        // (NO_RECORD_EXIST) hatası fırlatmıyoruz.
        // Aktif bir aboneliğin olmaması bir "hata" değil, "durum"dur.
        boolean hasActiveSubscription = subscriptionRepository.findByUserIdAndIsActive(currentUser.getId(), true)
                .isPresent();

        if (!hasActiveSubscription) {
            log.info("Heartbeat başarısız: Aktif abonelik bulunamadı. Kullanıcı: {}", currentUser.getUsername());
            return new HeartbeatResponse(false, "NO_ACTIVE_SUBSCRIPTION");
        }

        // 3. Tüm kontroller başarılı
        log.debug("Heartbeat başarılı: Kullanıcı: {}", currentUser.getUsername());
        return new HeartbeatResponse(true, "SESSION_ACTIVE");
    }
}
