package com.celalabaci.service;

import com.celalabaci.dto.announcement.AnnouncementDto;
import com.celalabaci.dto.announcement.AnnouncementCreateUpdateDto;
import java.util.List;

public interface IAnnouncementService {
    List<AnnouncementDto> getAllVisibleAnnouncements();
    List<AnnouncementDto> getAllAnnouncementsForAdmin();
    AnnouncementDto getAnnouncementById(Long id);
    AnnouncementDto createAnnouncement(AnnouncementCreateUpdateDto dto);
    AnnouncementDto updateAnnouncement(Long id, AnnouncementCreateUpdateDto dto);
    void deleteAnnouncement(Long id);
}
