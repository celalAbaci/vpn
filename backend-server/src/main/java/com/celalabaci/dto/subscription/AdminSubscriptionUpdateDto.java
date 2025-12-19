package com.celalabaci.dto.subscription;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AdminSubscriptionUpdateDto {

    @NotNull(message = "isActive status cannot be null.")
    private Boolean isActive;

    @NotNull(message = "End date cannot be null.")
    @Future(message = "End date must be in the future.")
    private LocalDate endDate;
}
