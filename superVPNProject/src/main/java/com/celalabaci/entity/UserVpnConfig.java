package com.celalabaci.entity;

import com.celalabaci.dto.config.VpnProtocol;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_vpn_configs")
public class UserVpnConfig extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = true)
    private UserDevice device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private VpnServer server;

    @Enumerated(EnumType.STRING)
    @Column(name = "protocol", nullable = false)
    private VpnProtocol protocol;

    @Lob
    @Column(name = "config_content", nullable = false, columnDefinition = "TEXT")
    private String configContent;

    @Column(name = "identifier_key")
    private String identifierKey;

    // --- EKLENEN KISIM ---
    // Bu alan eksik olduğu için hata alıyordun.
    @Column(name = "is_active")
    private boolean active = true;
}