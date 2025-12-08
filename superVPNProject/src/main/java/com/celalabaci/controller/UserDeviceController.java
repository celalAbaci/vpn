package com.celalabaci.controller;

import com.celalabaci.common.ApiResponse;
import com.celalabaci.dto.userdevice.UserDeviceCreateDto;
import com.celalabaci.dto.userdevice.UserDeviceDto;
import com.celalabaci.dto.userdevice.UserDeviceUpdateDto;
import com.celalabaci.entity.User;
import com.celalabaci.service.IUserDeviceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
public class UserDeviceController {

    @Autowired
    private IUserDeviceService userDeviceService;

    // --- USER Endpoints ---

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'PREMIUM')")
    public ResponseEntity<ApiResponse<List<UserDeviceDto>>> getMyDevices(@AuthenticationPrincipal User currentUser) {
        List<UserDeviceDto> devices = userDeviceService.getMyDevices(currentUser);
        return ResponseEntity.ok(ApiResponse.success(devices));
    }

    @PostMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'PREMIUM')")
    public ResponseEntity<ApiResponse<UserDeviceDto>> registerMyDevice(@Valid @RequestBody UserDeviceCreateDto dto, @AuthenticationPrincipal User currentUser) {
        UserDeviceDto newDevice = userDeviceService.registerMyDevice(dto, currentUser);
        return new ResponseEntity<>(ApiResponse.success("Device registered successfully.", newDevice), HttpStatus.CREATED);
    }

    @PostMapping("/guest")
    public ResponseEntity<ApiResponse<UserDeviceDto>> registerGuestDevice(@Valid @RequestBody UserDeviceCreateDto dto) {
        UserDeviceDto newDevice = userDeviceService.registerMyDevice(dto, null);
        return new ResponseEntity<>(ApiResponse.success("Guest device registered successfully.", newDevice), HttpStatus.CREATED);
    }

    @PutMapping("/my/{id}")
    @PreAuthorize("hasAnyRole('USER', 'PREMIUM')")
    public ResponseEntity<ApiResponse<UserDeviceDto>> updateMyDevice(@PathVariable Long id, @Valid @RequestBody UserDeviceUpdateDto dto, @AuthenticationPrincipal User currentUser) {
        UserDeviceDto updatedDevice = userDeviceService.updateMyDevice(id, dto, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Device updated successfully.", updatedDevice));
    }

    @DeleteMapping("/my/{id}")
    @PreAuthorize("hasAnyRole('USER', 'PREMIUM')")
    public ResponseEntity<ApiResponse<Void>> deleteMyDevice(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        userDeviceService.deleteMyDevice(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Device deleted successfully.", null));
    }

    // --- ADMIN Endpoints ---

    @GetMapping("/admin/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDeviceDto>>> getDevicesByUser(@PathVariable Long userId) {
        List<UserDeviceDto> devices = userDeviceService.getDevicesByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(devices));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDeviceByAdmin(@PathVariable Long id) {
        userDeviceService.deleteDeviceByAdmin(id);
        return ResponseEntity.ok(ApiResponse.success("Device deleted successfully by admin.", null));
    }
}
