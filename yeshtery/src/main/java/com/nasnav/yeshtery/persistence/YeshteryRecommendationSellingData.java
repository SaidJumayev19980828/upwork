package com.nasnav.yeshtery.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class YeshteryRecommendationSellingData {
    private Long ProductId;
    private String ProductName;
    private Long TotalCount;

    public YeshteryRecommendationSellingData(Long ProductId, String ProductName, Long TotalCount) {
        this.ProductId = ProductId;
        this.ProductName = ProductName;
        this.TotalCount = TotalCount;
    }
}
