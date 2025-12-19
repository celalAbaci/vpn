package com.celalabaci.service.impl;

import com.celalabaci.dto.userdevice.UserDeviceCreateDto;
import com.celalabaci.dto.userdevice.UserDeviceDto;
import com.celalabaci.dto.userdevice.UserDeviceUpdateDto;
import com.celalabaci.entity.User;
import com.celalabaci.entity.UserDevice;
import com.celalabaci.exception.BaseException;
import com.celalabaci.exception.MessageType;
import com.celalabaci.mapper.UserDeviceMapper;
import com.celalabaci.repository.UserDeviceRepository;
import com.celalabaci.service.IUserDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDeviceServiceImpl implements IUserDeviceService {

    @Autowired
    private UserDeviceRepository userDeviceRepository;

    @Autowired
    private UserDeviceMapper userDeviceMapper;

    @Override
    public List<UserDeviceDto> getMyDevices(User currentUser) {
        return userDeviceRepository.findByUserId(currentUser.getId()).stream()
                .map(userDeviceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDeviceDto registerMyDevice(UserDeviceCreateDto dto, User currentUser) {
        if (userDeviceRepository.existsByUserIdAndDeviceNameIgnoreCase(currentUser.getId(), dto.getDeviceName())) {
            throw new BaseException(MessageType.GENERAL_EXCEPTION, "You already have a device with this name.");
        }
        UserDevice newDevice = userDeviceMapper.toEntity(dto);
        newDevice.setUser(currentUser);
        newDevice.setLastSeen(OffsetDateTime.now());
        newDevice.setActive(true);

        UserDevice savedDevice = userDeviceRepository.save(newDevice);
        return userDeviceMapper.toDto(savedDevice);
    }

    @Override
    public UserDeviceDto updateMyDevice(Long deviceId, UserDeviceUpdateDto dto, User currentUser) {
        UserDevice device = userDeviceRepository.findByIdAndUserId(deviceId, currentUser.getId())
                .orElseThrow(() -> new BaseException(MessageType.NO_RECORD_EXIST, "Device not found or you don't have permission to access it."));

        userDeviceMapper.updateEntityFromDto(dto, device);
        UserDevice updatedDevice = userDeviceRepository.save(device);
        return userDeviceMapper.toDto(updatedDevice);
    }

    @Override
    public void deleteMyDevice(Long deviceId, User currentUser) {
        UserDevice device = userDeviceRepository.findByIdAndUserId(deviceId, currentUser.getId())
                .orElseThrow(() -> new BaseException(MessageType.NO_RECORD_EXIST, "Device not found or you don't have permission to delete it."));
        userDeviceRepository.delete(device);
    }

    @Override
    public List<UserDeviceDto> getDevicesByUserId(Long userId) {
        return userDeviceRepository.findByUserId(userId).stream()
                .map(userDeviceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteDeviceByAdmin(Long deviceId) {
        if (!userDeviceRepository.existsById(deviceId)) {
            throw new BaseException(MessageType.NO_RECORD_EXIST, "Device with id " + deviceId + " not found.");
        }
        userDeviceRepository.deleteById(deviceId);
    }
}
