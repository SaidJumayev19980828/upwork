package com.nasnav.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.enumerations.LoyaltyPointType;
import com.nasnav.enumerations.LoyaltyTransactions;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

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
    private BigDecimal cashBackPercentage;
    private Map<LoyaltyTransactions, BigDecimal> constraints;
    private String operation;
}
