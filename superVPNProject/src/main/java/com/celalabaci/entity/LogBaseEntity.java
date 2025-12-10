package com.celalabaci.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Getter
@Setter
@MappedSuperclass
public abstract class LogBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        // Log kayıtları için sadece oluşturulma zamanını UTC olarak kaydet.
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}