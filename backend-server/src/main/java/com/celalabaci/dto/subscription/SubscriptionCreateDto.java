package com.celalabaci.dto.subscription;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCreateDto {

    @NotNull(message = "Plan ID cannot be null.")
    private Long planId;
}
