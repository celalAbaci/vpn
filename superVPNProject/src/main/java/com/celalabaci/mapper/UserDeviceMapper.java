package com.celalabaci.mapper;

import com.celalabaci.dto.userdevice.UserDeviceDto;
import com.celalabaci.dto.userdevice.UserDeviceCreateDto;
import com.celalabaci.dto.userdevice.UserDeviceUpdateDto;
import com.celalabaci.entity.UserDevice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface UserDeviceMapper {

    UserDeviceDto toDto(UserDevice userDevice);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "lastSeen", ignore = true)
    @Mapping(target = "active", ignore = true) // HATA BURADAYDI, DÜZELTİLDİ
    UserDevice toEntity(UserDeviceCreateDto dto);

    void updateEntityFromDto(UserDeviceUpdateDto dto, @MappingTarget UserDevice userDevice);
}

