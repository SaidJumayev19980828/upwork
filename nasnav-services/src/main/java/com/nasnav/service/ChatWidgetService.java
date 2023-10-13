package com.nasnav.service;

import com.nasnav.dto.response.ChatWidgetSettingResponse;
import com.nasnav.dto.response.CreateChatWidgetRequest;
import com.nasnav.persistence.ChatWidgetSettingEntity;

public interface ChatWidgetService {

    ChatWidgetSettingResponse create(CreateChatWidgetRequest request);
    ChatWidgetSettingResponse publish(Long orgId);

    ChatWidgetSettingResponse getPublished(Long orgId);
    ChatWidgetSettingResponse getUnPublished(Long orgId);
}
