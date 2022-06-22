package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AppliedPromotionsResponse {
    private BigDecimal totalDiscount;
    private List<AppliedPromo> appliedPromos;
    private String error;

    public AppliedPromotionsResponse() {
        totalDiscount = BigDecimal.ZERO;
        appliedPromos = new ArrayList<>();
        error = "";
    }
}
