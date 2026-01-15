package com.celalabaci.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuestLoginRequest {

    @NotBlank(message = "Device ID is required")
    private String uniqueDeviceId;

    private String deviceName;
}
