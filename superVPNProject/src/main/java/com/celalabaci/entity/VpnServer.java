package com.celalabaci.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "vpn_servers")
public class VpnServer extends BaseEntity {

    @Column(name = "server_name", nullable = false, length = 100)
    private String serverName;

    @Column(name = "server_ip_address", nullable = false, unique = true, length = 45)
    private String serverIpAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    // --- SSH ve API Erişim Bilgileri (YENİ) ---
    @Column(name = "ssh_username", nullable = false)
    private String sshUsername = "root"; // Varsayılan root

    @Column(name = "ssh_password")
    private String sshPassword; // Şifreli bağlantı için

    @Column(name = "ssh_port")
    private Integer sshPort = 22;

    @Column(name = "admin_api_port")
    private Integer adminApiPort; // X-UI veya WireGuard paneli için port (örn: 2053, 51821)

    // --- Sunucu Yük İzleme ---
    @Column(name = "current_load_percentage")
    private Float currentLoadPercentage = 0.0f;

    @Column(name = "current_connected_users")
    private Integer currentConnectedUsers = 0;

    @Column(name = "is_active")
    private boolean active = true; // Changed from isActive to active to match Repository methods usually

    @Column(name = "is_free")
    private boolean isFree = false;
}
