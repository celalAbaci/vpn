package com.celalabaci.service.impl;

import com.celalabaci.dto.log.UserConnectionLogCreateDto;
import com.celalabaci.dto.log.UserConnectionLogDto;
import com.celalabaci.entity.Device;
import com.celalabaci.entity.User;
import com.celalabaci.entity.UserConnectionLog;
import com.celalabaci.entity.UserDevice;
import com.celalabaci.entity.VpnServer;
import com.celalabaci.exception.BaseException;
import com.celalabaci.exception.MessageType;
import com.celalabaci.mapper.UserConnectionLogMapper;
import com.celalabaci.repository.DeviceRepository;
import com.celalabaci.repository.UserConnectionLogRepository;
import com.celalabaci.repository.UserDeviceRepository;
import com.celalabaci.repository.VpnServerRepository;
import com.celalabaci.service.IUserConnectionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserConnectionLogServiceImpl implements IUserConnectionLogService {

    @Autowired
    private UserConnectionLogRepository logRepository;
    @Autowired
    private UserDeviceRepository userDeviceRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private VpnServerRepository serverRepository;
    @Autowired
    private UserConnectionLogMapper logMapper;

    @Override
    public List<UserConnectionLogDto> getMyConnectionLogs(User currentUser) {
        if (currentUser == null) return List.of();
        return logRepository.findByUserIdOrderByConnectTimeDesc(currentUser.getId()).stream()
                .map(logMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserConnectionLogDto createConnectionLog(UserConnectionLogCreateDto dto, User currentUser) {
        VpnServer server = serverRepository.findById(dto.getServerId())
                .orElseThrow(() -> new BaseException(MessageType.NO_RECORD_EXIST, "VPN Server with id " + dto.getServerId() + " not found."));

        UserConnectionLog log = new UserConnectionLog();
        log.setServer(server);
        log.setConnectTime(dto.getConnectTime());
        log.setDisconnectTime(dto.getDisconnectTime());
        log.setDataUsedMb(dto.getDataUsedMb());

        if (currentUser != null) {
            // Logged-in User
            if (dto.getDeviceId() == null) {
                 throw new BaseException(MessageType.VALIDATION_ERROR, "Device ID is required for logged-in users.");
            }
            UserDevice device = userDeviceRepository.findById(dto.getDeviceId())
                    .orElseThrow(() -> new BaseException(MessageType.NO_RECORD_EXIST, "Device with id " + dto.getDeviceId() + " not found."));

            if (!device.getUser().getId().equals(currentUser.getId())) {
                throw new BaseException(MessageType.GENERAL_EXCEPTION, "You can only create logs for your own devices.");
            }
            log.setUser(currentUser);
            log.setDevice(device);
        } else {
            // Guest User
            if (dto.getUniqueDeviceId() == null) {
                throw new BaseException(MessageType.VALIDATION_ERROR, "Unique Device ID is required for guest users.");
            }
            Device guestDevice = deviceRepository.findByUniqueDeviceId(dto.getUniqueDeviceId())
                    .orElseThrow(() -> new BaseException(MessageType.NO_RECORD_EXIST, "Guest Device not found."));

            log.setUser(null);
            log.setDeviceRef(guestDevice);
        }

        UserConnectionLog savedLog = logRepository.save(log);
        return logMapper.toDto(savedLog);
    }

    @Override
    public List<UserConnectionLogDto> getAllConnectionLogs() {
        return logRepository.findAll().stream()
                .map(logMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserConnectionLogDto> getConnectionLogsByUserId(Long userId) {
        return logRepository.findByUserIdOrderByConnectTimeDesc(userId).stream()
                .map(logMapper::toDto)
                .collect(Collectors.toList());
    }
}
