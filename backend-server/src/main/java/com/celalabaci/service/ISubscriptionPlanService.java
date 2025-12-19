package com.celalabaci.service;

import com.celalabaci.dto.subscriptionplan.SubscriptionPlanDto;
import com.celalabaci.dto.subscriptionplan.SubscriptionPlanCreateUpdateDto;
import java.util.List;

public interface ISubscriptionPlanService {
    List<SubscriptionPlanDto> getAllPlans();
    SubscriptionPlanDto getPlanById(Long id);
    SubscriptionPlanDto createPlan(SubscriptionPlanCreateUpdateDto dto);
    SubscriptionPlanDto updatePlan(Long id, SubscriptionPlanCreateUpdateDto dto);
    void deletePlan(Long id);
}
