package com.celalabaci.mapper;

import com.celalabaci.dto.subscription.SubscriptionDto;
import com.celalabaci.entity.Subscription;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class, SubscriptionPlanMapper.class})
public interface SubscriptionMapper {

    SubscriptionDto toDto(Subscription subscription);
}
