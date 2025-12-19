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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private VpnServer server;

    @Enumerated(EnumType.STRING)
    @Column(name = "protocol", nullable = false)
    private VpnProtocol protocol;

    // Config dosyasının içeriği (OVPN metni, WireGuard conf vb.)
    // TEXT tipinde tutulmalı çünkü uzun olabilir.
    @Lob
    @Column(name = "config_content", nullable = false, columnDefinition = "TEXT")
    private String configContent;

    // V2Ray için UUID veya WireGuard için Public Key gibi ek bilgiler
    @Column(name = "identifier_key")
    private String identifierKey;
}