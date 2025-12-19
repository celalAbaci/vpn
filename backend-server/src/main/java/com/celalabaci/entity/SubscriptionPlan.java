package com.celalabaci.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan extends BaseEntity {

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "speed_limit_mbps", nullable = false)
    private Integer speedLimitMbps;

    // YENİ EKLENEN ALANLAR
    @Column(name = "device_limit", nullable = false)
    private Integer deviceLimit; // Aynı anda bağlanabilecek cihaz sayısı

    @Column(name = "data_limit_gb", nullable = false)
    private Integer dataLimitGb; // Aylık veri limiti (GB cinsinden)
}
