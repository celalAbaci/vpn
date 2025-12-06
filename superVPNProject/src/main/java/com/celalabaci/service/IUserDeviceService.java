package com.celalabaci.service;

import com.celalabaci.dto.userdevice.UserDeviceCreateDto;
import com.celalabaci.dto.userdevice.UserDeviceDto;
import com.celalabaci.dto.userdevice.UserDeviceUpdateDto;
import com.celalabaci.entity.User;

import java.util.List;

public interface IUserDeviceService {
    // User methods
    List<UserDeviceDto> getMyDevices(User currentUser);
    UserDeviceDto registerMyDevice(UserDeviceCreateDto dto, User currentUser);
    UserDeviceDto updateMyDevice(Long deviceId, UserDeviceUpdateDto dto, User currentUser);
    void deleteMyDevice(Long deviceId, User currentUser);

    // Admin methods
    List<UserDeviceDto> getDevicesByUserId(Long userId);
    void deleteDeviceByAdmin(Long deviceId);
}
