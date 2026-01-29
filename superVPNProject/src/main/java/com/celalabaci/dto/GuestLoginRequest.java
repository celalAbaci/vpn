package com.celalabaci.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GuestLoginRequest {
    private String uniqueDeviceId;
    private String deviceName;
}
