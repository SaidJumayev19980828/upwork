package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
public class Prices {
    @JsonProperty("product_id")
    private Long productId;
    @JsonProperty("min_price")
    private BigDecimal minPrice;
    @JsonProperty("max_price")
    private BigDecimal maxPrice;

    public Prices(Long productId, BigDecimal minPrice, BigDecimal maxPrice) {
        this.productId = productId;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

}
