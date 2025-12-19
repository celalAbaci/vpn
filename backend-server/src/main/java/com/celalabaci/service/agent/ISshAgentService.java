package com.celalabaci.service.agent;

/**
 * VPN sunucularıyla SSH üzerinden iletişim kurmayı soyutlayan arayüz.
 * Bu, VpnApiAgentService'in sorumluluğunu azaltır ve mimariyi temizler.
 */
public interface ISshAgentService {

    /**
     * Belirtilen IP adresindeki sunucuya SSH ile bağlanır.
     *
     * @param serverIp Sunucu IP adresi.
     * @param username SSH kullanıcı adı.
     * @param privateKey SSH özel anahtar (veya parola).
     * @throws com.celalabaci.exception.SshConnectionException Bağlantı başarısız olursa.
     */
    void connect(String serverIp, String username, String privateKey);

    /**
     * Bağlı sunucu üzerinde bir komut çalıştırır.
     *
     * @param command Çalıştırılacak komut (örn: "wg set ...").
     * @return Komutun çıktısını (stdout) döner.
     * @throws com.celalabaci.exception.SshConnectionException Komut başarısız olursa veya bağlantı yoksa.
     */
    String runCommand(String command);

    /**
     * SSH bağlantısını kapatır.
     */
    void disconnect();

    /**
     * Tek seferlik bir komutu çalıştırıp bağlantıyı kapatan yardımcı metot.
     *
     * @param serverIp Sunucu IP adresi.
     * @param username SSH kullanıcı adı.
     * @param privateKey SSH özel anahtar.
     * @param command Çalıştırılacak komut.
     * @return Komutun çıktısı.
     * @throws com.celalabaci.exception.SshConnectionException Bağlantı veya komut başarısız olursa.
     */
    String runSingleCommand(String serverIp, String username, String privateKey, String command);
}
