package de.blinkt.openvpn.core;

public interface OpenVPNManagement {

    // VPN'i durdur
    boolean stopVPN(boolean replaceConnection);

    // Yeniden bağlan
    void reconnect();

    // Şifre/Challenge cevabı gönder
    void sendCRResponse(String response);

    // Duraklatma sebepleri (Enum)
    enum pauseReason {
        userPause,
        screenOff,
        noNetwork
    }

    // Duraklat
    void pause(pauseReason reason);

    // Devam ettir
    void resume();

    // Duraklatıldığında VPN durmalı mı?
    boolean stopVPNOnPause();

    // Ağ değişikliği
    void networkChange(boolean sameNetwork);
}