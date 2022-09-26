package com.nasnav.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.LoyaltyConfigConstraint;
import com.nasnav.enumerations.LoyaltyPointType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class LoyaltyPointConfigDTO {
    private Long id;
    private String description;
    private Long orgId;
    private Map<LoyaltyPointType, LoyaltyConfigConstraint> constraints;
    private Boolean isActive;
    private LoyaltyTierDTO defaultTier;
}
