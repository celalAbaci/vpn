package com.celalabaci.dto.subscriptionplan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer durationDays;
    private Integer speedLimitMbps;

    // YENİ EKLENEN ALANLAR
    private Integer deviceLimit;
    private Integer dataLimitGb;
    private String currency;
}
