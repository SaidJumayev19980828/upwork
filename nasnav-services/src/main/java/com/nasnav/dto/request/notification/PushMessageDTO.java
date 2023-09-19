package com.nasnav.dto.request.notification;

import com.nasnav.enumerations.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PushMessageDTO<T> {
    private final String title;
    private final T body;
    private final NotificationType type;
    public PushMessageDTO(String title, T body) {
        this(title, body, NotificationType.GENERIC);
    }
}
