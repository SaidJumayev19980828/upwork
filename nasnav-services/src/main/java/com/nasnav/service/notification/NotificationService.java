package com.nasnav.service.notification;

import com.nasnav.dto.request.notification.NotificationRequestDto;
import com.nasnav.persistence.BaseUserEntity;

public interface NotificationService {

    public void sendMessage(BaseUserEntity user, NotificationRequestDto notification);
}
