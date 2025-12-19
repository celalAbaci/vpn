package com.celalabaci.dto.announcement;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class AnnouncementCreateUpdateDto {
    @NotEmpty(message = "Title cannot be empty")
    private String title;

    @NotEmpty(message = "Message cannot be empty")
    private String message;

    private boolean isVisible = true;
    private String targetUrl;
}