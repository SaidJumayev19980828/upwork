package com.nasnav.service.notification;

import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.nasnav.dto.request.notification.NotificationRequestDto;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.service.SecurityService;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final FirebaseMessaging firebaseMessaging;
    private final SecurityService securityService;

    public NotificationServiceImpl(Optional<FirebaseMessaging> optionalFirebaseMessaging, SecurityService securityService) {
        firebaseMessaging = optionalFirebaseMessaging.orElse(null);
        this.securityService = securityService;
    }

    @Override
    public void sendMessage(BaseUserEntity user, NotificationRequestDto notifications)
            throws FirebaseMessagingException, FirebaseNotInitializedException {
        if (firebaseMessaging == null) {
            throw new FirebaseNotInitializedException();
        }
        Set<String> notificationTokens = securityService.getValidNotificationTokens(user);
        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(notificationTokens)
                .setNotification(new Notification(notifications.getTitle(), notifications.getBody()))
                .putData("content", notifications.getTitle())
                .putData("body", notifications.getBody())
                .build();
        firebaseMessaging.sendMulticast(message);

    }
}
