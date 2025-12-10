package com.celalabaci.controller;

import com.celalabaci.common.ApiResponse;
import com.celalabaci.dto.statistics.DashboardStatisticsDto;
import com.celalabaci.service.IAdminStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Bu controller'daki tüm endpoint'ler sadece ADMIN erişimine açık
public class AdminStatisticsController {

    private final IAdminStatisticsService statisticsService;

    /**
     * Admin paneli ana sayfası için temel istatistikleri döndürür.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatisticsDto>> getDashboardStatistics() {
        DashboardStatisticsDto stats = statisticsService.getDashboardStatistics();
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved successfully.", stats));
    }

}
