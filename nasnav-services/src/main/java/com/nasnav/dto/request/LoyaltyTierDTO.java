package com.nasnav.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class LoyaltyTierDTO {

    private Long id;
    private String tierName;
    private Boolean isActive;
    private Boolean isSpecial;
    private Integer noOfPurchaseFrom;
    private Integer noOfPurchaseTo;
    private Integer sellingPrice;
    private Long orgId;
    private Long boosterId;
    private BigDecimal cashBackPercentage;
    private BigDecimal coefficient;
    private String operation;
}
