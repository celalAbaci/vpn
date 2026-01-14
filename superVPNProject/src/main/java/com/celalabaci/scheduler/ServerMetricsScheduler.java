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

    @Scheduled(fixedRate = 300000)
    public void updateServerMetrics() {
        log.info("Sunucu metrikleri güncelleniyor...");

        // Önceki düzeltme: findByActiveTrue()
        List<VpnServer> activeServers = vpnServerRepository.findByActiveTrue();

        for (VpnServer server : activeServers) {
            try {
                String cpuCmd = "top -bn1 | grep \"Cpu(s)\" | awk '{print $2 + $4}'";
                String ramCmd = "free -m | awk 'NR==2{printf \"%.2f\", $3*100/$2 }'";

                if(server.getServerIpAddress() == null || server.getSshUsername() == null) continue;

                String cpuRes = sshAgentService.runSingleCommand(server.getServerIpAddress(), server.getSshUsername(), server.getSshPassword(), cpuCmd);
                String ramRes = sshAgentService.runSingleCommand(server.getServerIpAddress(), server.getSshUsername(), server.getSshPassword(), ramCmd);

                try {
                    float cpu = Float.parseFloat(cpuRes.trim().replace(",", "."));
                    float ram = Float.parseFloat(ramRes.trim().replace(",", "."));

                    // --- DÜZELTME BURADA ---
                    // (double) yerine (float) yaptık. Entity Float bekliyor.
                    server.setCurrentLoadPercentage((float) ((cpu + ram) / 2));

                } catch (Exception nfe) {
                    log.warn("Metrik parse hatası Server: {}", server.getServerName());
                }

                vpnServerRepository.save(server);

            } catch (Exception e) {
                log.error("Sunucu metrik güncelleme hatası ({}): {}", server.getServerName(), e.getMessage());
            }
        }
    }
}