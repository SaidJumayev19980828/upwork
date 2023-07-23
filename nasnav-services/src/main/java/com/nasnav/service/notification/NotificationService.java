package com.nasnav.service.notification;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.nasnav.dto.request.notification.NotificationRequestDto;
import com.nasnav.persistence.BaseUserEntity;

public interface NotificationService {
    public class FirebaseNotInitializedException extends Exception {}

    public void sendMessage(BaseUserEntity user, NotificationRequestDto notification)
            throws FirebaseMessagingException, FirebaseNotInitializedException;
}
