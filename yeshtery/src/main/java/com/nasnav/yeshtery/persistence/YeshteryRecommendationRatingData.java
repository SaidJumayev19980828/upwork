package com.nasnav.yeshtery.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class YeshteryRecommendationRatingData {
    private Long ProductId;
    private Long TagId;
    private String ProductName;
    private Long TotalCount;
    private Long TotalRate;
    private int Rate;

    public YeshteryRecommendationRatingData(Long ProductId, Long TagId, String ProductName, Long TotalCount, Long TotalRate, int Rate) {
        this.ProductId = ProductId;
        this.TagId = TagId;
        this.ProductName = ProductName;
        this.TotalCount = TotalCount;
        this.TotalRate = TotalRate;
        this.Rate = Rate;
    }
}
