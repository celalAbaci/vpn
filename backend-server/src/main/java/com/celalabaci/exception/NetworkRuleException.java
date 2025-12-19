package com.celalabaci.exception;

/**
 * Sunucu (iptables) veya istemci (firewall) üzerinde ağ kuralları
 * eklenirken/kaldırılırken bir hata oluştuğunda fırlatılacak
 * özel exception sınıfı.
 */
public class NetworkRuleException extends BaseException {

    public NetworkRuleException(MessageType messageType, String detail) {
        super(messageType, detail);
    }
}
