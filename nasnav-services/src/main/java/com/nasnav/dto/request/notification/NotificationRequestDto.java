package com.nasnav.dto.request.notification;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotificationRequestDto {
    private String target;
    private String title;
    private String body;

    public NotificationRequestDto(String target, String title, String body) {
        this.target = target;
        this.title = title;
        this.body = body;
    }

    public NotificationRequestDto(String target) {
        this.target = target;
        this.title = "Default Title";
        this.body = "Default Body";
    }
}
