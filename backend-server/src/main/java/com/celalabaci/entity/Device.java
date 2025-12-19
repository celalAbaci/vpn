package com.celalabaci.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unique_device_id", unique = true, nullable = false)
    private String uniqueDeviceId;

    @Column(name = "first_seen")
    @CreationTimestamp
    private LocalDateTime firstSeen;

    @Column(name = "last_seen")
    @UpdateTimestamp
    private LocalDateTime lastSeen;

    // Optional: Link to a user if they eventually log in
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_banned")
    private boolean isBanned = false;
}
