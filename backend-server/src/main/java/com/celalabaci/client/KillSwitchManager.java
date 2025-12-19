package com.celalabaci.client;

import com.celalabaci.exception.MessageType;
import com.celalabaci.exception.NetworkRuleException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder;

/**
 * İSTEMCİ (CLIENT) TARAFLI Kill Switch Yöneticisi (Konseptüel Sınıf).
 *
 * BU KOD SPRING BOOT BACKEND'DE ÇALIŞMAZ.
 * Bu sınıf, bir Windows istemci makinesinde (örn: JavaFX ile yazılmış bir
 * VPN istemcisinde) çalışacak şekilde tasarlanmıştır.
 * Windows Güvenlik Duvarı'nı (netsh) kullanarak ağ kuralları ekler ve kaldırır.
 */
public class KillSwitchManager {

    private static final String RULE_NAME_BLOCK_OUT = "SuperVPN-KillSwitch-Block-Out";
    private static final String RULE_NAME_ALLOW_VPN = "SuperVPN-KillSwitch-Allow-VPN";

    /**
     * Kill Switch'i etkinleştirir.
     * Tüm giden trafiği engeller, SADECE VPN sunucusuna izin verir.
     *
     * @param vpnServerIp İzin verilecek VPN sunucusunun IP adresi.
     * @throws NetworkRuleException Komutlar başarısız olursa.
     */
    public void enable(String vpnServerIp) {
        System.out.println("Kill Switch ETKİNLEŞTİRİLİYOR. Sadece " + vpnServerIp + " adresine izin verilecek.");
        try {
            // Önce mevcut kuralları temizle
            disable();

            // 1. Kural: VPN sunucusuna giden trafiğe İZİN VER
            // Not: Gerçekte UDP/TCP ve Port (51820, 1194 vb.) belirtmek daha doğrudur.
            String allowVpnCmd = String.format(
                    "netsh advfirewall firewall add rule name=\"%s\" dir=out action=allow remoteip=%s",
                    RULE_NAME_ALLOW_VPN,
                    vpnServerIp
            );
            runCommand(allowVpnCmd);

            // 2. Kural: Geri kalan TÜM giden trafiği ENGELLE
            String blockAllCmd = String.format(
                    "netsh advfirewall firewall add rule name=\"%s\" dir=out action=block",
                    RULE_NAME_BLOCK_OUT
            );
            runCommand(blockAllCmd);

            System.out.println("Kill Switch BAŞARIYLA ETKİNLEŞTİRİLDİ.");

        } catch (Exception e) {
            System.err.println("Kill Switch etkinleştirilemedi: " + e.getMessage());
            // Başarısız olursa, güvenliği sağlamak için kuralları tekrar temizle
            disable();
            throw new NetworkRuleException(MessageType.NETWORK_RULE_FAILED, "Kill Switch etkinleştirilemedi: " + e.getMessage());
        }
    }

    /**
     * Kill Switch'i devre dışı bırakır.
     * Eklenen tüm kuralları temizler ve normal internet erişimini geri getirir.
     *
     * @throws NetworkRuleException Komutlar başarısız olursa.
     */
    public void disable() {
        System.out.println("Kill Switch DEVRE DIŞI BIRAKILIYOR...");
        try {
            // Eklediğimiz kuralları isimle sil
            runCommand("netsh advfirewall firewall delete rule name=\"" + RULE_NAME_ALLOW_VPN + "\"");
            runCommand("netsh advfirewall firewall delete rule name=\"" + RULE_NAME_BLOCK_OUT + "\"");
            System.out.println("Kill Switch başarıyla DEVRE DIŞI BIRAKILDI.");
        } catch (Exception e) {
            // Silme hatası genellikle kural yoksa oluşur, bu göz ardı edilebilir.
            System.err.println("Kill Switch kuralları temizlenirken hata oluştu (normal olabilir): " + e.getMessage());
        }
    }

    /**
     * Windows (cmd) üzerinde bir komut çalıştıran yardımcı metot.
     *
     * @param command Çalıştırılacak komut.
     * @throws Exception Başarısız olursa.
     */
    private void runCommand(String command) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            // "No rules match the specified criteria" hatası silme işlemi için normaldir.
            if (command.startsWith("netsh advfirewall firewall delete") && output.toString().contains("No rules")) {
                System.out.println("Temizleme: Kural zaten yok: " + command);
                return; // Hata fırlatma
            }
            throw new Exception("Komut başarısız oldu (Exit Code: " + exitCode + "). Çıktı: " + output);
        }
        System.out.println("Komut başarılı: " + command);
    }

    // Bu sınıfı test etmek için konseptüel bir main metodu
    public static void main(String[] args) {
        KillSwitchManager ksm = new KillSwitchManager();
        try {
            // Test için VPN sunucu IP'si
            String vpnIp = "8.8.8.8"; // Google DNS'i test için kullanalım

            // 1. Etkinleştir
            ksm.enable(vpnIp);
            System.out.println("Test: Kill Switch AÇIK. Sadece 8.8.8.8'e ping atılabilmeli, google.com'a atılamamalı.");
            Thread.sleep(10000); // 10 saniye bekle

            // 2. Devre dışı bırak
            ksm.disable();
            System.out.println("Test: Kill Switch KAPALI. google.com'a tekrar ping atılabilmeli.");

        } catch (Exception e) {
            e.printStackTrace();
            // Başarısızlık durumunda her zaman disable() çağır
            ksm.disable();
        }
    }
}
