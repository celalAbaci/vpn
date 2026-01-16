package com.celalabaci.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GuestLoginRequest {
    @NotBlank(message = "Unique Device ID is mandatory")
    private String uniqueDeviceId;

    private String deviceName;
}
