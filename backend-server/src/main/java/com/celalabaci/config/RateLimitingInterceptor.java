package com.celalabaci.config;

import com.celalabaci.exception.MessageType;
import com.celalabaci.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IP bazlı rate limiting uygulayan Spring Handler Interceptor.
 * Bucket4j kütüphanesini kullanarak her bir IP adresi için istek kotalarını yönetir.
 */
@Component
@Slf4j
public class RateLimitingInterceptor implements HandlerInterceptor {

    // application.properties'den gelen ayarlar
    private final long capacity;
    private final long refillRate;
    private final Duration refillPeriod;

    // Her bir IP adresi için bir "Bucket" (istek kotası) saklar.
    // ConcurrentHashMap, birden fazla thread'in (isteğin) aynı anda güvenle erişebilmesi için gereklidir.
    private final Map<String, Bucket> ipCache = new ConcurrentHashMap<>();

    // Ayarları application.properties'den enjekte et
    public RateLimitingInterceptor(
            @Value("${security.rate-limit.auth.capacity}") long capacity,
            @Value("${security.rate-limit.auth.refill-rate}") long refillRate,
            @Value("${security.rate-limit.auth.refill-period-minutes}") long refillPeriodMinutes) {

        this.capacity = capacity;
        this.refillRate = refillRate;
        this.refillPeriod = Duration.ofMinutes(refillPeriodMinutes);

        log.info("Rate Limiting (Auth) Başlatıldı: Kapasite={}, Yenileme={}/{}dk",
                capacity, refillRate, refillPeriodMinutes);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String ipAddress = getClientIp(request);

        // Bu IP için bir bucket (kota) al veya oluştur.
        Bucket ipBucket = ipCache.computeIfAbsent(ipAddress, this::createNewBucket);

        // Bu IP'nin 1 istek yapma hakkı olup olmadığını kontrol et.
        ConsumptionProbe probe = ipBucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // İstek kabul edildi. Kalan hakları yanıta ekleyebiliriz (opsiyonel).
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true; // true -> Controller metoduna devam et
        } else {
            // İstek reddedildi (Limit aşıldı).

            // İstemciye ne kadar beklemesi gerektiğini saniye cinsinden hesapla.
            long secondsToWait = Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds();
            if (secondsToWait == 0) {
                secondsToWait = 1; // 0 saniye ise en az 1 saniye beklemesini söyle
            }

            log.warn("Rate limit aşıldı. IP: {}. {} saniye beklemesi gerekiyor.", ipAddress, secondsToWait);

            // Projenin özel istisna mimarisine uygun hatayı fırlat.
            // Bu istisna GlobalExceptionHandler tarafından yakalanacak ve 429 (Too Many Requests) dönecek.
            throw new RateLimitExceededException(
                    MessageType.TOO_MANY_REQUESTS,
                    "IP: " + ipAddress,
                    secondsToWait
            );
        }
    }

    /**
     * Verilen IP adresi için application.properties'deki ayarlara göre yeni bir Bucket oluşturur.
     */
    private Bucket createNewBucket(String ipAddress) {
        Refill refill = Refill.greedy(refillRate, refillPeriod);
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * İsteği yapan kullanıcının IP adresini alır.
     * (Proxy/Load balancer arkasındaysa X-Forwarded-For başlığını kontrol eder)
     */
    private String getClientIp(HttpServletRequest request) {
        String xffHeader = request.getHeader("X-Forwarded-For");
        if (xffHeader != null && !xffHeader.isEmpty()) {
            // X-Forwarded-For başlığı "client, proxy1, proxy2" şeklinde olabilir.
            // Genellikle ilk IP, gerçek istemci IP'sidir.
            return xffHeader.split(",")[0].trim();
        }
        // Eğer XFF başlığı yoksa, doğrudan isteğin IP adresini al.
        return request.getRemoteAddr();
    }
}
