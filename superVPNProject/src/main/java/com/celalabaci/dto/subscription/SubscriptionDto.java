package com.celalabaci.dto.subscription;

import com.celalabaci.dto.subscriptionplan.SubscriptionPlanDto;
import com.celalabaci.dto.user.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDto {
    private Long id;
    private UserDto user;
    private SubscriptionPlanDto plan;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer speedLimitMbps;
    private boolean isActive;
}
