package com.nasnav.dto.response;

import com.nasnav.enumerations.ChatSettingType;
import lombok.Data;

@Data
public class ChatWidgetSettingResponse {
    private Long id;
    private String value;
    private Integer type;
    private Long organizationId;
}
