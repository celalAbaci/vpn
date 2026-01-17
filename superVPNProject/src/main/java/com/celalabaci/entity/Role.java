package com.celalabaci.entity;

public enum Role {
    USER,
    ADMIN,
    PREMIUM,
    MODERATOR,
    GUEST // Added for internal logic handling, though guests might not have a User entity
}
