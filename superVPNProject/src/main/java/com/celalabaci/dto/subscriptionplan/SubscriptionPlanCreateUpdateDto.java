package com.celalabaci.dto.subscriptionplan;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanCreateUpdateDto {

    @NotEmpty(message = "Plan name cannot be empty.")
    private String name;

    @NotNull(message = "Price cannot be null.")
    @Positive(message = "Price must be positive.")
    private BigDecimal price;

    @NotNull(message = "Duration cannot be null.")
    @Positive(message = "Duration in days must be positive.")
    private Integer durationDays;

    @NotNull(message = "Speed limit cannot be null.")
    @Positive(message = "Speed limit in Mbps must be positive.")
    private Integer speedLimitMbps;

    // YENİ EKLENEN ALANLAR (Exception mimarisine uygun validasyon)
    @NotNull(message = "Device limit cannot be null.")
    @Positive(message = "Device limit must be positive.")
    private Integer deviceLimit;

    @NotNull(message = "Data limit cannot be null.")
    @Positive(message = "Data limit in GB must be positive.")
    private Integer dataLimitGb;
}
