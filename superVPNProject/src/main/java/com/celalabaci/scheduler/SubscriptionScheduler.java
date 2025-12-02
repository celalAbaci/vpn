package com.celalabaci.scheduler;

import com.celalabaci.entity.Subscription;
import com.celalabaci.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Abonelik durumlarını kontrol eden zamanlanmış görevleri içerir.
 * Bu sınıf, süresi dolan abonelikleri periyodik olarak pasif hale getirir.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final SubscriptionRepository subscriptionRepository;

    /**
     * Süresi dolan abonelikleri pasifleştirmek için zamanlanmış görevi tetikler.
     * Cron expression: Her gün gece 00:01'de çalışır.
     */
    @Scheduled(cron = "0 1 0 * * ?")
    public void scheduleDeactivateExpiredSubscriptions() {
        log.info("ZAMANLANMIŞ GÖREV BAŞLADI: Süresi dolan abonelikler kontrol ediliyor...");
        try {
            // Asıl işi yapan transactional metodu çağır
            deactivateExpiredSubscriptions();
        } catch (Exception e) {
            // Görevin kendisi bir hata fırlatırsa, bu durum loglanır ve
            // Spring'in zamanlayıcısının bir sonraki çalıştırmada tekrar denemesi sağlanır.
            // Projenizdeki BaseException veya diğer özel exception'ları burada yakalayabilirsiniz.
            log.error("Zamanlanmış abonelik pasifleştirme görevinde beklenmedik bir hata oluştu.", e);
        }
    }

    /**
     * Süresi dolan (endDate'i bugünden önce olan) ve hala aktif (isActive=true)
     * olan abonelikleri bulur ve bunları pasif (isActive=false) hale getirir.
     * Bu metot, bir hata oluşursa işlemi geri almak (rollback) için @Transactional olarak işaretlenmiştir.
     * Bu, "exception mimarisine uygunluk" talebinizi karşılar.
     */
    @Transactional(rollbackFor = Exception.class)
    public void deactivateExpiredSubscriptions() {
        // Kontrolü bugünün başlangıcına göre yapıyoruz.
        // Bitiş tarihi 'dün' (veya daha eskisi) olanları pasif yapar.
        LocalDate today = LocalDate.now();

        List<Subscription> expiredSubscriptions = subscriptionRepository.findByEndDateBeforeAndIsActiveTrue(today);

        if (expiredSubscriptions.isEmpty()) {
            log.info("Pasif hale getirilecek süresi dolmuş abonelik bulunamadı.");
            return;
        }

        log.info("{} adet süresi dolmuş abonelik bulundu. Pasif hale getiriliyor...", expiredSubscriptions.size());

        for (Subscription sub : expiredSubscriptions) {
            sub.setActive(false);
            log.debug("Abonelik pasif hale getirildi: ID {}, Kullanıcı ID: {}, Bitiş Tarihi: {}",
                    sub.getId(), sub.getUser().getId(), sub.getEndDate());
        }

        // Değişiklikleri toplu halde veritabanına kaydet
        subscriptionRepository.saveAll(expiredSubscriptions);

        log.info("ZAMANLANMIŞ GÖREV TAMAMLANDI. {} adet abonelik başarıyla pasif hale getirildi.", expiredSubscriptions.size());
    }
}
