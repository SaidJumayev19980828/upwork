package com.nasnav.dto.request.notification;

import lombok.Data;

@Data
public class NotificationRequestDto {
    private String target;
    private String title;
    private String body;
}
