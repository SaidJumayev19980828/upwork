package com.nasnav.dto.request.notification;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubscriptionRequestDto {
    private String topicName;
    private String token;
}
