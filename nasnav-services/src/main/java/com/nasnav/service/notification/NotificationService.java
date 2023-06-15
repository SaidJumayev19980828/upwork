package com.nasnav.service.notification;

import com.nasnav.dto.request.notification.NotificationRequestDto;

public interface NotificationService {

    public void sendMessage(NotificationRequestDto notifications);
}
