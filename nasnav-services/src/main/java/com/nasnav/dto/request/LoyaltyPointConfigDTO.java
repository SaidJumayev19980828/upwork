package com.nasnav.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class LoyaltyPointConfigDTO {
    private Long id;
    private String description;
    private Long shopId;
    private Integer amountFrom;
    private Integer amountTo;
    private Integer points;
    private BigDecimal ratioFrom;
    private BigDecimal ratioTo;
    private BigDecimal coefficient;
    private Boolean isActive;
}
