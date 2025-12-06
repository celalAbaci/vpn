package com.celalabaci.service.impl;


import com.celalabaci.dto.announcement.AnnouncementDto;
import com.celalabaci.dto.announcement.AnnouncementCreateUpdateDto;
import com.celalabaci.entity.Announcement;
import com.celalabaci.exception.BaseException;
import com.celalabaci.exception.MessageType;
import com.celalabaci.mapper.AnnouncementMapper;
import com.celalabaci.repository.AnnouncementRepository;
import com.celalabaci.service.IAnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementServiceImpl implements IAnnouncementService {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    public List<AnnouncementDto> getAllVisibleAnnouncements() {
        return announcementRepository.findByIsVisibleTrue().stream()
                .map(announcementMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnouncementDto> getAllAnnouncementsForAdmin() {
        return announcementRepository.findAll().stream()
                .map(announcementMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AnnouncementDto getAnnouncementById(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BaseException(MessageType.NO_RECORD_EXIST, "Announcement with id " + id + " not found."));
        return announcementMapper.toDto(announcement);
    }

    @Override
    public AnnouncementDto createAnnouncement(AnnouncementCreateUpdateDto dto) {
        Announcement announcement = announcementMapper.toEntity(dto);
        Announcement savedAnnouncement = announcementRepository.save(announcement);
        return announcementMapper.toDto(savedAnnouncement);
    }

    @Override
    public AnnouncementDto updateAnnouncement(Long id, AnnouncementCreateUpdateDto dto) {
        Announcement existingAnnouncement = announcementRepository.findById(id)
                .orElseThrow(() -> new BaseException(MessageType.NO_RECORD_EXIST, "Announcement with id " + id + " not found."));

        announcementMapper.updateEntityFromDto(dto, existingAnnouncement);

        Announcement updatedAnnouncement = announcementRepository.save(existingAnnouncement);
        return announcementMapper.toDto(updatedAnnouncement);
    }

    @Override
    public void deleteAnnouncement(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new BaseException(MessageType.NO_RECORD_EXIST, "Announcement with id " + id + " not found.");
        }
        announcementRepository.deleteById(id);
    }
}

