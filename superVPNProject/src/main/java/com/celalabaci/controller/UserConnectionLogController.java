package com.celalabaci.controller;

import com.celalabaci.common.ApiResponse;
import com.celalabaci.dto.log.UserConnectionLogCreateDto;
import com.celalabaci.dto.log.UserConnectionLogDto;
import com.celalabaci.entity.User;
import com.celalabaci.service.IUserConnectionLogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/logs")
public class UserConnectionLogController {

    @Autowired
    private IUserConnectionLogService logService;

    // --- USER Endpoints ---

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<UserConnectionLogDto>>> getMyLogs(@AuthenticationPrincipal User currentUser) {
        List<UserConnectionLogDto> logs = logService.getMyConnectionLogs(currentUser);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @PostMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserConnectionLogDto>> createLog(@Valid @RequestBody UserConnectionLogCreateDto dto, @AuthenticationPrincipal User currentUser) {
        UserConnectionLogDto createdLog = logService.createConnectionLog(dto, currentUser);
        return new ResponseEntity<>(ApiResponse.success("Connection log created successfully.", createdLog), HttpStatus.CREATED);
    }

    // --- ADMIN Endpoints ---

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserConnectionLogDto>>> getAllLogs() {
        List<UserConnectionLogDto> logs = logService.getAllConnectionLogs();
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/admin/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserConnectionLogDto>>> getLogsByUser(@PathVariable Long userId) {
        List<UserConnectionLogDto> logs = logService.getConnectionLogsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
