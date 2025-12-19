package com.celalabaci.dto.announcement;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class AnnouncementDto {
    private Long id;
    private String title;
    private String message;
    private boolean isVisible;
    private String targetUrl;
    private OffsetDateTime createdAt;
}
