package com.nasnav.test;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.nasnav.dto.request.notification.NotificationRequestDto;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.SecurityService;
import com.nasnav.service.notification.NotificationService;
import com.nasnav.service.notification.NotificationServiceImpl;
import com.nasnav.service.notification.NotificationService.FirebaseNotInitializedException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class NotificationTest {
    @Mock
    SecurityService securityService;
    @Mock
    FirebaseMessaging firebaseMessaging;
    NotificationService unInitializedNotificationService = new NotificationServiceImpl(Optional.empty(), securityService);
    NotificationService notificationService;

    @BeforeEach
    void init() {
        notificationService = new NotificationServiceImpl(Optional.of(firebaseMessaging), securityService);
    }

    @Test
    void tryToSendUnInitialized() {
        final BaseUserEntity user = new UserEntity();
        final NotificationRequestDto notificationRequestDto = new NotificationRequestDto();
        assertThrows(NotificationService.FirebaseNotInitializedException.class, () -> {
            unInitializedNotificationService.sendMessage(user, notificationRequestDto);
        });
    }

    @Test
    void sendMessage() throws FirebaseMessagingException, FirebaseNotInitializedException {
        BaseUserEntity user = new UserEntity();
        Set<String> tokens = Set.of("token1", "token2");
        NotificationRequestDto notificationRequestDto = new NotificationRequestDto();
        notificationRequestDto.setTitle("some-title");
        notificationRequestDto.setBody("some-body");
        Mockito.when(securityService.getValidNotificationTokens(user)).thenReturn(tokens);
        notificationService.sendMessage(user, notificationRequestDto);
        Mockito.verify(firebaseMessaging).sendMulticast(any(MulticastMessage.class));
    }
}
