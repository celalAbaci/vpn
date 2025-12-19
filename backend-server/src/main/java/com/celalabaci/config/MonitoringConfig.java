package com.celalabaci.config;

import com.celalabaci.service.agent.VpnApiAgentService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gelişmiş izleme (Monitoring) yapılandırmalarını içeren sınıf.
 * Özel Health Indicator'ları ve Metrik'leri burada tanımlarız.
 */
@Configuration
@RequiredArgsConstructor
public class MonitoringConfig {

    private final VpnApiAgentService vpnApiAgentService;

    /**
     * '/actuator/health' endpoint'ine 'vpnAgent' adında özel bir sağlık kontrolü ekler.
     * Bu kontrol, VPN sunucularıyla konuşan mock servisin "çalışıp çalışmadığını"
     * (ping metodunu çağırarak) kontrol eder.
     * <p>
     * Gerçek bir senaryoda bu, VPN sunucularına SSH veya API ile
     * bağlanılıp bağlanılamadığını test edebilirdi.
     *
     * @return HealthIndicator bileşeni.
     */
    @Bean
    public HealthIndicator vpnAgentHealthIndicator() {
        return () -> {
            try {
                // Mock servisin ping metodunu çağır
                boolean isUp = vpnApiAgentService.ping();
                if (isUp) {
                    return Health.up()
                            .withDetail("message", "VPN Agent (Mock) is responsive.")
                            .build();
                } else {
                    // Bu durum, mock servisin kasıtlı olarak 'false' dönmesi durumunda olur
                    return Health.down()
                            .withDetail("message", "VPN Agent (Mock) is not responsive.")
                            .build();
                }
            } catch (Exception e) {
                // Gerçek senaryoda burası SSH bağlantı hatası vb. olabilir.
                // Bu durum, exception mimarisine uygun bir örnektir.
                return Health.down(e)
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    /**
     * Uygulama genelinde özel metrikler (counter, gauge, timer) oluşturabilmek için
     * MeterRegistry'yi bir Spring Bean'i olarak tanımlar.
     * Spring Boot normalde bunu otomatik yapar, ancak test edilebilirlik ve
     * explicit yapılandırma için bu yararlıdır.
     *
     * @return MeterRegistry
     */
    @Bean
    public MeterRegistry meterRegistry() {
        // Basit bir registry. Prometheus bunu alıp kendi formatına çevirecektir.
        return new SimpleMeterRegistry();
    }
}
