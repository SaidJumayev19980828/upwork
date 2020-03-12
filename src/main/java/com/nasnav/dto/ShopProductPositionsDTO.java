package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ShopProductPositionsDTO {
    @JsonProperty("view360_id")
    private Long view360Id;

    @JsonProperty("product_positions")
    private String productPositions;
}
