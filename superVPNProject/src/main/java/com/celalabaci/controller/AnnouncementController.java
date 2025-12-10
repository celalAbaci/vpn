package com.celalabaci.controller;


import com.celalabaci.common.ApiResponse;
import com.celalabaci.dto.announcement.AnnouncementDto;
import com.celalabaci.dto.announcement.AnnouncementCreateUpdateDto;
import com.celalabaci.service.IAnnouncementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/announcements")
public class AnnouncementController {

    @Autowired
    private IAnnouncementService announcementService;

    // Herkesin erişebileceği endpoint
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'PREMIUM')")
    public ResponseEntity<ApiResponse<List<AnnouncementDto>>> getVisibleAnnouncements() {
        List<AnnouncementDto> announcements = announcementService.getAllVisibleAnnouncements();
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }

    // Sadece admin'in erişebileceği endpoint (tüm duyurular)
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AnnouncementDto>>> getAllAnnouncements() {
        List<AnnouncementDto> announcements = announcementService.getAllAnnouncementsForAdmin();
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'PREMIUM')")
    public ResponseEntity<ApiResponse<AnnouncementDto>> getAnnouncementById(@PathVariable Long id) {
        AnnouncementDto announcement = announcementService.getAnnouncementById(id);
        return ResponseEntity.ok(ApiResponse.success(announcement));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AnnouncementDto>> createAnnouncement(@Valid @RequestBody AnnouncementCreateUpdateDto dto) {
        AnnouncementDto createdAnnouncement = announcementService.createAnnouncement(dto);
        return new ResponseEntity<>(ApiResponse.success("Announcement created successfully.", createdAnnouncement), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AnnouncementDto>> updateAnnouncement(@PathVariable Long id, @Valid @RequestBody AnnouncementCreateUpdateDto dto) {
        AnnouncementDto updatedAnnouncement = announcementService.updateAnnouncement(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Announcement updated successfully.", updatedAnnouncement));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.ok(ApiResponse.success("Announcement deleted successfully.", null));
    }
}
