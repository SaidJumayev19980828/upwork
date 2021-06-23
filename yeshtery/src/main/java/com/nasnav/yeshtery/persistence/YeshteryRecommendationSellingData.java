package com.nasnav.yeshtery.persistence;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class YeshteryRecommendationSellingData {
    private Long ProductId;
    private Long TagId;
    private String ProductName;
    private Long TotalCount;
}
