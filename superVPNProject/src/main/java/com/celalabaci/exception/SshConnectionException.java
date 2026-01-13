package com.celalabaci.exception;

/**
 * VPN sunucusuna SSH bağlantısı kurulurken veya komut çalıştırılırken
 * bir hata oluştuğunda fırlatılacak özel exception sınıfı.
 * Mevcut exception mimarisine uyar.
 */
public class SshConnectionException extends BaseException {

    public SshConnectionException(MessageType messageType, String detail) {
        super(messageType, detail);
    }
}
