package com.nasnav.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TierDTO {

    private Long id;
    private String tierName;
    private Boolean isActive;
    private Boolean isSpecial;
    private Integer noOfPurchaseFrom;
    private Integer noOfPurchaseTo;
    private Integer sellingPrice;
    private Long orgId;
    private Long boosterId;
}
