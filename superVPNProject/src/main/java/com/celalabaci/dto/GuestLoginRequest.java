package com.celalabaci.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuestLoginRequest {
    @NotBlank(message = "Unique Device ID cannot be empty")
    private String uniqueDeviceId;
}
