package com.celalabaci.mapper;

import com.celalabaci.dto.vpnserver.VpnServerDto;
import com.celalabaci.dto.vpnserver.VpnServerCreateUpdateDto;
import com.celalabaci.entity.VpnServer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {CountryMapper.class})
public interface VpnServerMapper {

    VpnServerDto toDto(VpnServer vpnServer);

    // DTO'daki countryId'nin entity'deki Country nesnesine doğrudan maplenemeyeceğini belirtiyoruz.
    // Bu atamayı service katmanında manuel yapacağız.
    @Mapping(target = "country", ignore = true)
    VpnServer toEntity(VpnServerCreateUpdateDto dto);

    @Mapping(target = "country", ignore = true)
    void updateEntityFromDto(VpnServerCreateUpdateDto dto, @MappingTarget VpnServer vpnServer);
}
