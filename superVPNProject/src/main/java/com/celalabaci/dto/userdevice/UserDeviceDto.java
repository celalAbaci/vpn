package com.celalabaci.dto.userdevice;

import com.celalabaci.dto.user.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDeviceDto {
    private Long id;
    private UserDto user;
    private String deviceName;
    private OffsetDateTime lastSeen;
    private boolean active;
}
