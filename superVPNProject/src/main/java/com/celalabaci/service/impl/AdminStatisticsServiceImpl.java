package com.celalabaci.service.impl;

import com.celalabaci.dto.statistics.DashboardStatisticsDto;
import com.celalabaci.repository.PaymentRepository;
import com.celalabaci.repository.SubscriptionRepository;
import com.celalabaci.repository.UserConnectionLogRepository;
import com.celalabaci.repository.UserRepository;
import com.celalabaci.repository.VpnServerRepository; // YENİ IMPORT
import com.celalabaci.service.IAdminStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class AdminStatisticsServiceImpl implements IAdminStatisticsService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final UserConnectionLogRepository logRepository;
    private final VpnServerRepository vpnServerRepository; // YENİ INJECT

    @Override
    public DashboardStatisticsDto getDashboardStatistics() {
        // Bugünün başlangıcı (UTC)
        OffsetDateTime todayStart = OffsetDateTime.now(ZoneOffset.UTC).toLocalDate().atStartOfDay().atOffset(ZoneOffset.UTC);

        // Mevcut istatistikler
        long totalUsers = userRepository.count();
        long newUsersToday = userRepository.countByCreatedAtAfter(todayStart);
        long activeSubscriptions = subscriptionRepository.countByIsActive(true);

        // Değerler null gelirse 0 olarak ayarla
        BigDecimal totalRevenue = paymentRepository.findTotalRevenue();
        BigDecimal totalDataUsed = logRepository.findTotalDataUsedMb();

        // --- YENİ EKLENEN İSTATİSTİKLER ---
        Double avgLoad = vpnServerRepository.findAverageActiveServerLoad();
        Integer totalConnected = vpnServerRepository.findTotalActiveConnectedUsers();

        return new DashboardStatisticsDto(
                totalUsers,
                newUsersToday,
                activeSubscriptions,
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO, // Null check
                totalDataUsed != null ? totalDataUsed : BigDecimal.ZERO, // Null check
                avgLoad != null ? avgLoad : 0.0, // Null check
                totalConnected != null ? totalConnected : 0 // Null check
        );
    }
}
