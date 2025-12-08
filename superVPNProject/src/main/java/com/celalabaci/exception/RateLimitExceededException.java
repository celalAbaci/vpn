package com.celalabaci.exception;

import lombok.Getter;

/**
 * Rate limit (istek limiti) aşıldığında fırlatılacak özel istisna sınıfı.
 * Projenin mevcut BaseException mimarisine uyar.
 */
@Getter
public class RateLimitExceededException extends BaseException {

    private final long retryAfterSeconds;

    /**
     * @param messageType Özel MessageType (örn: TOO_MANY_REQUESTS)
     * @param detail Detay mesajı (örn: IP adresi)
     * @param retryAfterSeconds İsteğin ne kadar süre sonra tekrar denenebileceği (saniye cinsinden)
     */
    public RateLimitExceededException(MessageType messageType, String detail, long retryAfterSeconds) {
        super(messageType, detail);
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
