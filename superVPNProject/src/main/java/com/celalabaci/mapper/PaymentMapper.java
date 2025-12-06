package com.celalabaci.mapper;

import com.celalabaci.dto.payment.PaymentDto;
import com.celalabaci.entity.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class, SubscriptionMapper.class})
public interface PaymentMapper {

    PaymentDto toDto(Payment payment);

}
