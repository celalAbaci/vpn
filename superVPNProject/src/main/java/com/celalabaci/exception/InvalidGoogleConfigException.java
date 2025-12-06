package com.celalabaci.exception;

/**
 * GooglePaymentConfig başlatılamazsa fırlatılacak özel runtime exception.
 * Bu, BaseException'dan farklıdır çünkü uygulamanın BAŞLANGICINI durdurmalıdır.
 */
public class InvalidGoogleConfigException extends RuntimeException {
    private final MessageType messageType;
    private final String detail;

    public InvalidGoogleConfigException(MessageType messageType, String detail) {
        super(messageType.getMessage() + ": " + detail);
        this.messageType = messageType;
        this.detail = detail;
    }
}
