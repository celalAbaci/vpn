package com.celalabaci.dto;

import com.celalabaci.entity.Role;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private OffsetDateTime createdAt;
}

