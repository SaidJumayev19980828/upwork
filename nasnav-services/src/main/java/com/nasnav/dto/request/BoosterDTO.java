package com.nasnav.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class BoosterDTO {

    private Long id;
    private String boosterName;
    private Integer linkedFamilyMember;
    private Boolean isActive;
    private Integer numberFamilyChildren;
    private Integer purchaseSize;
    private Integer reviewProducts;
    private Integer numberPurchaseOffline;
    private Integer socialMediaReviews;
    private Long orgId;
    private Integer levelBooster;
    private Integer activationMonths;
}
