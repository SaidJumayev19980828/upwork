package com.nasnav.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.nasnav.dto.request.notification.PushMessageDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.SecurityService;
import com.nasnav.service.notification.NotificationService;
import com.nasnav.service.notification.NotificationServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ExtendWith(MockitoExtension.class)
class NotificationTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    SecurityService securityService;
    @Mock
    FirebaseMessaging firebaseMessaging;
    NotificationService unInitializedNotificationService = new NotificationServiceImpl(Optional.empty(),
            securityService, objectMapper);
    NotificationService notificationService;

    @Captor
    ArgumentCaptor<MulticastMessage> multicastMessageCaptor;

    @BeforeEach
    void init() {
        notificationService = new NotificationServiceImpl(Optional.of(firebaseMessaging), securityService,
                objectMapper);
    }

    @Test
    void tryToSendUnInitialized() {
        final BaseUserEntity user = new UserEntity();
        final PushMessageDTO<String> notificationRequestDto = getNotificationRequestDto();
        assertThrows(RuntimeBusinessException.class, () -> {
            unInitializedNotificationService.sendMessage(user, notificationRequestDto);
        });
    }

    @Test
    void sendStringMessage() throws FirebaseMessagingException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        BaseUserEntity user = new UserEntity();
        Set<String> tokens = Set.of("token1", "token2");
        PushMessageDTO<String> notificationRequestDto = getNotificationRequestDto();
        Mockito.when(securityService.getValidNotificationTokens(user)).thenReturn(tokens);
        notificationService.sendMessage(user, notificationRequestDto);
        Mockito.verify(firebaseMessaging).sendEachForMulticast(multicastMessageCaptor.capture());
        Map<String, String> data = getCaptorMessageData(multicastMessageCaptor);

        assertEquals("test body", data.get("body"));
    }

    @Test
    void sendIntegerMessage() throws FirebaseMessagingException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        BaseUserEntity user = new UserEntity();
        Set<String> tokens = Set.of("token1", "token2");
        PushMessageDTO<Integer> notificationRequestDto = new PushMessageDTO<>("some title", 58);
        Mockito.when(securityService.getValidNotificationTokens(user)).thenReturn(tokens);
        notificationService.sendMessage(user, notificationRequestDto);
        Mockito.verify(firebaseMessaging).sendEachForMulticast(multicastMessageCaptor.capture());
        Map<String, String> data = getCaptorMessageData(multicastMessageCaptor);

        assertEquals("58", data.get("body"));
    }

    @Test
    void sendMessageFailsNoToken() {
        BaseUserEntity user = new UserEntity();
        Set<String> tokens = Set.of();
        PushMessageDTO<String> notificationRequestDto = getNotificationRequestDto();
        Mockito.when(securityService.getValidNotificationTokens(user)).thenReturn(tokens);
        assertThrows(RuntimeBusinessException.class, () -> {
            notificationService.sendMessage(user, notificationRequestDto);
        });
    }

    
    @Test
    void multicastBatches() throws FirebaseMessagingException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        BaseUserEntity user = new UserEntity();
        Set<String> tokens = getSetOfTokens(800);
        PushMessageDTO<Integer> notificationRequestDto = new PushMessageDTO<>("some title", 58);
        Mockito.when(securityService.getValidNotificationTokens(user)).thenReturn(tokens);
        notificationService.sendMessage(user, notificationRequestDto);
        Mockito.verify(firebaseMessaging, times(2)).sendEachForMulticast(any());
    }

    @Test
    void sendToOrgEmployees() throws FirebaseMessagingException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        Set<String> tokens = getSetOfTokens(800);
        PushMessageDTO<Integer> notificationRequestDto = new PushMessageDTO<>("some title", 58);
        Mockito.when(securityService.getValidNotificationTokensForOrgEmployees(58L)).thenReturn(tokens);
        notificationService.sendMessageToOrganizationEmplyees(58L, notificationRequestDto);
        Mockito.verify(firebaseMessaging, times(2)).sendEachForMulticast(any());
    }

    @Test
    void sendToShopEmployees() throws FirebaseMessagingException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        Set<String> tokens = getSetOfTokens(800);
        PushMessageDTO<Integer> notificationRequestDto = new PushMessageDTO<>("some title", 58);
        Mockito.when(securityService.getValidNotificationTokensForShopEmployees(58L)).thenReturn(tokens);
        notificationService.sendMessageToShopEmplyees(58L, notificationRequestDto);
        Mockito.verify(firebaseMessaging, times(2)).sendEachForMulticast(any());
    }


    Set<String> getSetOfTokens(int count) {
        return IntStream.range(0, count).mapToObj((current) -> "token" + current).collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getCaptorMessageData(ArgumentCaptor<MulticastMessage> captor)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        MulticastMessage message = captor.getValue();

        // Create Field object
        Field privateField = MulticastMessage.class.getDeclaredField("data");

        // Set the accessibility as true
        privateField.setAccessible(true);

        return (Map<String, String>) privateField.get(message);
    }

    private PushMessageDTO<String> getNotificationRequestDto() {
        final PushMessageDTO<String> notificationRequestDto = new PushMessageDTO<>("test title", "test body");
        return notificationRequestDto;
    }
}
