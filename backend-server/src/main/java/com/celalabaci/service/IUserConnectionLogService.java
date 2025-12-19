package com.celalabaci.service;

import com.celalabaci.dto.log.UserConnectionLogCreateDto;
import com.celalabaci.dto.log.UserConnectionLogDto;
import com.celalabaci.entity.User;

import java.util.List;

public interface IUserConnectionLogService {
    // User method
    List<UserConnectionLogDto> getMyConnectionLogs(User currentUser);
    UserConnectionLogDto createConnectionLog(UserConnectionLogCreateDto dto, User currentUser);

    // Admin methods
    List<UserConnectionLogDto> getAllConnectionLogs();
    List<UserConnectionLogDto> getConnectionLogsByUserId(Long userId);
}
