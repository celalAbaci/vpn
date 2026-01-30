package com.celalabaci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuestAuthResponse {
    private String accessToken;
    private Long deviceId;
}
