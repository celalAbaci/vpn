package com.celalabaci.mapper;

import com.celalabaci.dto.log.UserConnectionLogDto;
import com.celalabaci.dto.userdevice.DeviceInfoDto;
import com.celalabaci.dto.vpnserver.ServerInfoDto;
import com.celalabaci.entity.UserConnectionLog;
import com.celalabaci.entity.UserDevice;
import com.celalabaci.entity.VpnServer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface UserConnectionLogMapper {

    UserConnectionLogDto toDto(UserConnectionLog log);

    // Helper methods to map entities to their Info DTOs
    DeviceInfoDto deviceToInfoDto(UserDevice device);
    ServerInfoDto serverToInfoDto(VpnServer server);
}
