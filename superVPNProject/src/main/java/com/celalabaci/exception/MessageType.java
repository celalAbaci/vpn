package com.celalabaci.exception;

import lombok.Getter;

@Getter
public enum MessageType {

    NO_RECORD_EXIST("1004", "Kayıt bulunamadı."),
    TOKEN_IS_EXPIRED("1005", "JWT süresi dolmuştur."),
    TOKEN_IS_INVALID("1006", "JWT formatı hatalı veya geçersizdir."),
    USERNAME_NOT_FOUND("1007", "Kullanıcı bulunamadı."),
    USERNAME_OR_PASSWORD_INVALID("1008", "Kullanıcı adı veya parola hatalı."),
    REFRESH_TOKEN_NOT_FOUND("1009", "Yenileme tokenı bulunamadı."),
    REFRESH_TOKEN_IS_EXPIRED("1010", "Yenileme tokenı süresi dolmuştur."),
    TOO_MANY_REQUESTS("1011", "Çok fazla deneme yapıldı. Lütfen daha sonra tekrar deneyin."),

    // VPN İÇİN EKLENENLER
    VPN_LIMIT_EXCEEDED("2001", "Kullanıcı VPN bağlantı limitini aştı."),
    VPN_CONFIG_NOT_ACTIVE("2002", "VPN konfigürasyonu aktif değil."),
    MULTI_HOP_NOT_SUPPORTED("2003", "Multi-hop (çift sunucu) bu protokol veya sunucu için desteklenmiyor."),
    MULTI_HOP_SERVERS_SAME("2004", "Multi-hop için giriş ve çıkış sunucuları aynı olamaz."),
    INVALID_DNS_PROVIDER("2005", "Geçersiz veya desteklenmeyen bir DNS sağlayıcı istendi."),

    // --- YENİ EKLENDİ: AGENT VE AĞ HATALARI ---
    SSH_CONNECTION_FAILED("2100", "Sunucuya SSH bağlantısı kurulamadı veya komut çalıştırılamadı."),
    NETWORK_RULE_FAILED("2101", "Sunucu veya istemci üzerinde ağ kuralı (iptables/firewall) ayarlanırken bir hata oluştu."),
    AGENT_PROVISIONING_FAILED("2102", "VPN Agent servisi kullanıcıyı (peer) oluştururken hata verdi."),

    // --- YENİ EKLENDİ: GOOGLE PLAY ÖDEME HATALARI ---
    GOOGLE_PLAY_VERIFICATION_FAILED("3001", "Google Play doğrulaması başarısız oldu."),
    PURCHASE_ALREADY_USED("3002", "Bu satın alma işlemi zaten kullanılmış veya geçersiz."),
    INVALID_GOOGLE_CONFIG("3003", "Google Play API yapılandırması eksik veya hatalı."),
    PLAN_NOT_FOUND_FOR_PRODUCT_ID("3004", "Sistemde bu Google Play ürün kimliğine karşılık gelen bir abonelik planı bulunamadı."),
    PAYMENT_TRANSACTION_FAILED("3005", "Ödeme kaydı oluşturulurken bir hata oluştu."),

    // --- BİTİŞ ---
    GENERAL_EXCEPTION("9999", "Genel bir hata oluştu.");

    private final String code;
    private final String message;

    MessageType(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
