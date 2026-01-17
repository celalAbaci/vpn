package com.celalabaci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuestLoginRequest {
    private String uniqueDeviceId;
    private String deviceName;
}
