package com.celalabaci.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Admin panelinin ana sayfası için temel istatistikleri içeren DTO.
 * YENİ EKLENEN ALANLAR: averageServerLoadPercentage, totalConnectedUsers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsDto {

    // Mevcut Alanlar
    private long totalUsers;
    private long newUsersToday;
    private long activeSubscriptions;
    private BigDecimal totalRevenue;
    private BigDecimal totalDataUsedMb;

    // --- YENİ EKLENEN ALANLAR ---

    /**
     * Tüm aktif sunucuların ortalama yük yüzdesi.
     */
    private Double averageServerLoadPercentage;

    /**
     * Tüm aktif sunuculardaki toplam bağlı kullanıcı sayısı.
     */
    private Integer totalConnectedUsers;
}
