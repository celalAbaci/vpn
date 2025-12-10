package com.celalabaci.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "announcements")
public class Announcement extends BaseEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_visible")
    private boolean isVisible = true;

    @Column(name = "target_url")
    private String targetUrl;
}
