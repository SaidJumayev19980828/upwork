package com.nasnav.dto.response;

import com.nasnav.enumerations.ChatSettingType;
import lombok.Data;

@Data
public class CreateChatWidgetRequest {
    private String value;
    private Long organizationId;
}
