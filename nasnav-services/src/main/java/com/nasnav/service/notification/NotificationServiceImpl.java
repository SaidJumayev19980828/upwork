package com.nasnav.service.notification;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.nasnav.commons.utils.CollectionUtils;
import com.nasnav.dto.request.notification.PushMessageDTO;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.service.SecurityService;

@Service

public class NotificationServiceImpl implements NotificationService {
    private final FirebaseMessaging firebaseMessaging;
    private final SecurityService securityService;
    private final ObjectMapper objectMapper;

    public NotificationServiceImpl(Optional<FirebaseMessaging> optionalFirebaseMessaging,
            SecurityService securityService, ObjectMapper objectMapper) {
        firebaseMessaging = optionalFirebaseMessaging.orElse(null);
        this.securityService = securityService;
        this.objectMapper = objectMapper;
    }

    // TODO: support async calls

    @Override
    public void sendMessage(BaseUserEntity user, PushMessageDTO<?> notification) {
        validateFirebaseMessagingUp();
        Set<String> notificationTokens = securityService.getValidNotificationTokens(user);
        if (notificationTokens.isEmpty()) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, ErrorCodes.NOTIF$0003, user.getId());
        }
        sendMulticast(notification, notificationTokens);

    }

    @Override
    public void sendMessageToOrganizationEmplyees(Long orgId, PushMessageDTO<?> notification) {
        validateFirebaseMessagingUp();
        Set<String> notificationTokens = securityService.getValidNotificationTokensForOrgEmployees(orgId);
        if (notificationTokens.isEmpty()) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, ErrorCodes.NOTIF$0004, orgId);
        }
        sendMulticast(notification, notificationTokens);

    }

    @Override
    public void sendMessageToShopEmplyees(Long shopId, PushMessageDTO<?> notification) {
        validateFirebaseMessagingUp();
        Set<String> notificationTokens = securityService.getValidNotificationTokensForShopEmployees(shopId);
        if (notificationTokens.isEmpty()) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, ErrorCodes.NOTIF$0005, shopId);
        }
        sendMulticast(notification, notificationTokens);

    }

    private void sendMulticast(PushMessageDTO<?> notificationDTO, Set<String> notificationTokens) {
        try {
            String body = notificationDTO.getBody() instanceof String ? (String) notificationDTO.getBody()
                    : objectMapper.writeValueAsString(notificationDTO.getBody());
            List<List<String>> batches = CollectionUtils.divideToBatches(notificationTokens, 500);
            for (var batch : batches) {
                sendBatchMulticast(notificationDTO.getTitle(), body, batch);
                
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, ErrorCodes.NOTIF$0006);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, ErrorCodes.NOTIF$0002, e.getErrorCode());
        }
    }

    private BatchResponse sendBatchMulticast(String title, String body,
            Collection<String> notificationTokens) throws FirebaseMessagingException {
        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(notificationTokens)
                .putData("title", title)
                .putData("body", body)
                .build();
        return firebaseMessaging.sendEachForMulticast(message);
    }

    private void validateFirebaseMessagingUp() {
        if (firebaseMessaging == null) {
            throw new RuntimeBusinessException(HttpStatus.SERVICE_UNAVAILABLE, ErrorCodes.NOTIF$0001);
        }
    }
}
