package com.nasnav.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class LoyaltyCoinsDropLogsDTO {
    private Long id;
    private Long orgId;
    private Long userId;
    private Long coinsDropId;
    private Boolean isActive;
}
