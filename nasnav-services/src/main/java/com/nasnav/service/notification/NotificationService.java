package com.nasnav.service.notification;

import com.nasnav.dto.request.notification.NotificationRequestDto;

import java.util.concurrent.ExecutionException;

public interface NotificationService {

    public void sendMessage(NotificationRequestDto notifications);
}
