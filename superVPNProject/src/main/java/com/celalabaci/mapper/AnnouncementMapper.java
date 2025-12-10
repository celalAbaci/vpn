package com.celalabaci.mapper;

import com.celalabaci.dto.announcement.AnnouncementDto;
import com.celalabaci.dto.announcement.AnnouncementCreateUpdateDto;
import com.celalabaci.entity.Announcement;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AnnouncementMapper {

    AnnouncementMapper INSTANCE = Mappers.getMapper(AnnouncementMapper.class);

    AnnouncementDto toDto(Announcement announcement);

    Announcement toEntity(AnnouncementCreateUpdateDto dto);

    void updateEntityFromDto(AnnouncementCreateUpdateDto dto, @MappingTarget Announcement announcement);
}