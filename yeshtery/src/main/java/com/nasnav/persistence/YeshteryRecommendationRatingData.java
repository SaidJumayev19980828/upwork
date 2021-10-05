package com.nasnav.persistence;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class YeshteryRecommendationRatingData {
    private Long ProductId;
    private String ProductName;
    private Long TotalCount;
    private Long TotalRate;
    private Long Rate;

    public YeshteryRecommendationRatingData(Long ProductId, String ProductName, Long TotalCount, Long TotalRate, Long Rate) {
        this.ProductId = ProductId;
        this.ProductName = ProductName;
        this.TotalCount = TotalCount;
        this.TotalRate = TotalRate;
        this.Rate = Rate;
    }
}
