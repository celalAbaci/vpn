package com.celalabaci.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.celalabaci.entity.User;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_connection_logs", indexes = {
        @Index(name = "idx_log_user", columnList = "user_id"),
        @Index(name = "idx_log_device", columnList = "device_id"),
        @Index(name = "idx_log_server", columnList = "server_id"),
        @Index(name = "idx_log_connect_time", columnList = "connect_time")
})
public class UserConnectionLog extends LogBaseEntity { // BaseEntity yerine LogBaseEntity kullanıldı

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private UserDevice device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private VpnServer server;

    @Column(name = "connect_time", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime connectTime;

    @Column(name = "disconnect_time", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime disconnectTime;

    @Column(name = "data_used_mb", precision = 10, scale = 2)
    private BigDecimal dataUsedMb;
}