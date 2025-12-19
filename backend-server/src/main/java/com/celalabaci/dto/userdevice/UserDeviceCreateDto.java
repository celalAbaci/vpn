package com.celalabaci.dto.userdevice;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDeviceCreateDto {
    @NotEmpty(message = "Device name cannot be empty.")
    private String deviceName;
}
