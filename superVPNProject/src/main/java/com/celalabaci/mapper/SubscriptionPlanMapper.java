package com.celalabaci.mapper;

import com.celalabaci.dto.subscriptionplan.SubscriptionPlanDto;
import com.celalabaci.dto.subscriptionplan.SubscriptionPlanCreateUpdateDto;
import com.celalabaci.entity.SubscriptionPlan;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SubscriptionPlanMapper {

    SubscriptionPlanDto toDto(SubscriptionPlan subscriptionPlan);

    SubscriptionPlan toEntity(SubscriptionPlanCreateUpdateDto dto);

    void updateEntityFromDto(SubscriptionPlanCreateUpdateDto dto, @MappingTarget SubscriptionPlan subscriptionPlan);
}
