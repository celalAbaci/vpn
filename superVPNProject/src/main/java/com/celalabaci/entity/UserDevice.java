package com.celalabaci.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.celalabaci.entity.User;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_devices")
public class UserDevice extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_name", nullable = false, length = 100)
    private String deviceName;

    @Column(name = "last_seen", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime lastSeen;

    @Column(name = "is_active")
    private boolean active = true;
}