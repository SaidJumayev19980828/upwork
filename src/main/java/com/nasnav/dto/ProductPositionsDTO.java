package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class ProductPositionsDTO {

    @JsonProperty("shop_id")
    private Long shopId;

    @JsonProperty("products_data")
    private Map<String, ProductPositionsDataDTO> productPositions;
}
