package com.nasnav.dto.request.notification;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotificationRequestDto {
    private String title;
    private String body;
    public NotificationRequestDto(String title, String body) {
        this.title = title;
        this.body = body;
    }
}
