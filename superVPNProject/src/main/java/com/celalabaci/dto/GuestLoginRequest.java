package com.celalabaci.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuestLoginRequest {
    @NotBlank
    private String uniqueDeviceId;
}
