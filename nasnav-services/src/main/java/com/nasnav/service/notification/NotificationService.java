package com.nasnav.service.notification;

import com.nasnav.dto.request.notification.PushMessageDTO;
import com.nasnav.persistence.BaseUserEntity;

public interface NotificationService {

    public void sendMessage(BaseUserEntity user, PushMessageDTO<?> notification);

    public void sendMessageToOrganizationEmplyees(Long orgId, PushMessageDTO<?> notification);

    public void sendMessageToShopEmplyees(Long shopId, PushMessageDTO<?> notification);
}
