package com.celalabaci.exception;

/**
 * VPN konfigürasyonu oluşturulurken bir hata oluştuğunda fırlatılacak
 * özel exception sınıfı.
 */
public class ConfigGenerationException extends BaseException {

    public ConfigGenerationException(MessageType messageType, String detail) {
        super(messageType, detail);
    }
}
