package com.celalabaci.service;

import com.celalabaci.dto.statistics.DashboardStatisticsDto;

/**
 * Admin paneli için istatistiksel verileri toplayan servis arayüzü.
 */
public interface IAdminStatisticsService {

    /**
     * Admin paneli ana sayfası için genel istatistikleri getirir.
     * @return DashboardStatisticsDto
     */
    DashboardStatisticsDto getDashboardStatistics();

}
