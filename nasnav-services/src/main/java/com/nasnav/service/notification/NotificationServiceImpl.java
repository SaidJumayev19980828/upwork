package com.nasnav.service.notification;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.nasnav.dto.request.notification.NotificationRequestDto;

import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final FirebaseMessaging firebaseMessaging;

    public NotificationServiceImpl(Optional<FirebaseMessaging> optionalFirebaseMessaging) {
        firebaseMessaging = optionalFirebaseMessaging.orElse(null);
    }

    @Override
    public void sendMessage(NotificationRequestDto notifications) {

        Message message = Message.builder()
                .setToken(notifications.getTarget())
                .setNotification(new Notification(notifications.getTitle(), notifications.getBody()))
                .putData("content", notifications.getTitle())
                .putData("body", notifications.getBody())
                .build();

        try {
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            log.error("Fail to send firebase notification", e);
        }

    }


}
