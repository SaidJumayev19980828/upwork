package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AppliedPointsResponse {
    private BigDecimal totalDiscount;
    private List<AppliedPoints> appliedPoints;

    public AppliedPointsResponse() {
        totalDiscount = BigDecimal.ZERO;
        appliedPoints = new ArrayList<>();
    }
}
