package com.celalabaci.dto.payment;

import com.celalabaci.dto.subscription.SubscriptionDto;
import com.celalabaci.dto.user.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private Long id;
    private UserDto user;
    // We can use the full SubscriptionDto to show detailed info about the sub
    private SubscriptionDto subscription;
    private BigDecimal amount;
    private OffsetDateTime paymentDate;
    private String transaction;
    private String status;
    private OffsetDateTime createdAt;
}
