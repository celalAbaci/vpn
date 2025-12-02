package com.celalabaci.mapper;

import com.celalabaci.dto.country.CountryDto;
import com.celalabaci.dto.country.CountryCreateUpdateDto;
import com.celalabaci.entity.Country;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CountryMapper {

    CountryDto toDto(Country country);

    Country toEntity(CountryCreateUpdateDto dto);

    void updateEntityFromDto(CountryCreateUpdateDto dto, @MappingTarget Country country);
}
