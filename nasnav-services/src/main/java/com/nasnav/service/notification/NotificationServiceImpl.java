package com.nasnav.service.notification;

import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.nasnav.dto.request.notification.NotificationRequestDto;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
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
    public void sendMessage(BaseUserEntity user, NotificationRequestDto notifications) {
        if (firebaseMessaging == null) {
            throw new RuntimeBusinessException(HttpStatus.SERVICE_UNAVAILABLE, ErrorCodes.NOTIF$0001);
        }
        Set<String> notificationTokens = securityService.getValidNotificationTokens(user);
        if (notificationTokens.isEmpty()) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, ErrorCodes.NOTIF$0003, user.getId());
        }
        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(notificationTokens)
                .setNotification(new Notification(notifications.getTitle(), notifications.getBody()))
                .putData("content", notifications.getTitle())
                .putData("body", notifications.getBody())
                .build();
        try {
            firebaseMessaging.sendMulticast(message);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, ErrorCodes.NOTIF$0002, e.getErrorCode());
        }

    }
}
