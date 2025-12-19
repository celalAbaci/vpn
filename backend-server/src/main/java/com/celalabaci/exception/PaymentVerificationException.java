package com.celalabaci.exception;

/**
 * Google Play veya diğer ödeme sistemi doğrulamaları sırasında oluşacak
 * hatalar için özelleştirilmiş exception sınıfı.
 * Mevcut BaseException yapısına uygundur.
 */
public class PaymentVerificationException extends BaseException {

    public PaymentVerificationException(MessageType messageType, String detail) {
        super(messageType, detail);
    }
}
