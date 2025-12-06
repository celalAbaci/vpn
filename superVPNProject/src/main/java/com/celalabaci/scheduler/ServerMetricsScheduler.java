package com.celalabaci.scheduler;

import com.celalabaci.entity.VpnServer;
import com.celalabaci.repository.VpnServerRepository;
import com.celalabaci.service.agent.ISshAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServerMetricsScheduler {

    private final VpnServerRepository vpnServerRepository;
    private final ISshAgentService sshAgentService;

    // Her 5 dakikada bir çalıştır (Sık çalıştırmak SSH'ı yorabilir)
    @Scheduled(fixedRate = 300000)
    public void updateServerMetrics() {
        log.info("Sunucu metrikleri güncelleniyor...");
        List<VpnServer> activeServers = vpnServerRepository.findByIsActiveTrue();

        for (VpnServer server : activeServers) {
            try {
                // CPU Komutu
                // top -bn1 | grep "Cpu(s)" | sed "s/.*, *\([0-9.]*\)%* id.*/\1/" | awk '{print 100 - $1}'
                // Çıktı formatı locale göre değişebilir, basit bir uptime komutu daha güvenlidir başlangıç için.
                String cpuCmd = "top -bn1 | grep \"Cpu(s)\" | awk '{print $2 + $4}'"; // Basitleştirilmiş usage

                // RAM Komutu
                // free -m | awk 'NR==2{printf "%.2f", $3*100/$2 }'
                String ramCmd = "free -m | awk 'NR==2{printf \"%.2f\", $3*100/$2 }'";

                String cpuRes = sshAgentService.runSingleCommand(server.getServerIpAddress(), server.getSshUsername(), server.getSshPassword(), cpuCmd);
                String ramRes = sshAgentService.runSingleCommand(server.getServerIpAddress(), server.getSshUsername(), server.getSshPassword(), ramCmd);

                // Parse işlemleri (Gelen veri string olduğu için float'a çevirirken dikkat edilmeli)
                try {
                    float cpu = Float.parseFloat(cpuRes.trim().replace(",", ".")); // Türkçe locale fix
                    // RAM verisini de benzer şekilde işleyebiliriz ama entity'de tek load alanı var, ortalamasını alalım.
                    float ram = Float.parseFloat(ramRes.trim().replace(",", "."));

                    server.setCurrentLoadPercentage((cpu + ram) / 2);
                } catch (NumberFormatException nfe) {
                    log.warn("Metrik parse hatası Server: {} - Veri: CPU:{} RAM:{}", server.getServerName(), cpuRes, ramRes);
                }

                // Bağlı kullanıcı sayısı (OpenVPN için örnek: openvpn-status.log dosyasını saymak gerekebilir)
                // Şimdilik 0 geçiyoruz veya özel bir script ile sayılabilir.

                vpnServerRepository.save(server);

            } catch (Exception e) {
                log.error("Sunucu metrik güncelleme hatası ({}): {}", server.getServerName(), e.getMessage());
            }
        }
    }
}