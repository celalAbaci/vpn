package com.celalabaci.mapper;

import com.celalabaci.dto.user.UserDto;
import com.celalabaci.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
}
