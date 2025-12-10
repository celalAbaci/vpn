package com.celalabaci.service.impl;

import com.celalabaci.dto.log.UserConnectionLogCreateDto;
import com.celalabaci.dto.log.UserConnectionLogDto;
import com.celalabaci.entity.User;
import com.celalabaci.entity.UserConnectionLog;
import com.celalabaci.entity.UserDevice;
import com.celalabaci.entity.VpnServer;
import com.celalabaci.exception.BaseException;
import com.celalabaci.exception.MessageType;
import com.celalabaci.mapper.UserConnectionLogMapper;
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
    private UserDeviceRepository deviceRepository;
    @Autowired
    private VpnServerRepository serverRepository;
    @Autowired
    private UserConnectionLogMapper logMapper;

    @Override
    public List<UserConnectionLogDto> getMyConnectionLogs(User currentUser) {
        return logRepository.findByUserIdOrderByConnectTimeDesc(currentUser.getId()).stream()
                .map(logMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserConnectionLogDto createConnectionLog(UserConnectionLogCreateDto dto, User currentUser) {
        // Find the device and verify it belongs to the current user
        UserDevice device = deviceRepository.findById(dto.getDeviceId())
                .orElseThrow(() -> new BaseException(MessageType.NO_RECORD_EXIST, "Device with id " + dto.getDeviceId() + " not found."));

        if (!device.getUser().getId().equals(currentUser.getId())) {
            throw new BaseException(MessageType.GENERAL_EXCEPTION, "You can only create logs for your own devices.");
        }

        VpnServer server = serverRepository.findById(dto.getServerId())
                .orElseThrow(() -> new BaseException(MessageType.NO_RECORD_EXIST, "VPN Server with id " + dto.getServerId() + " not found."));

        UserConnectionLog log = new UserConnectionLog();
        log.setUser(currentUser);
        log.setDevice(device);
        log.setServer(server);
        log.setConnectTime(dto.getConnectTime());
        log.setDisconnectTime(dto.getDisconnectTime());
        log.setDataUsedMb(dto.getDataUsedMb());

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
