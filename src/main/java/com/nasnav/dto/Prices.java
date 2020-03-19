package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Prices {
    @JsonProperty("min_price")
    private BigDecimal minPrice;
    @JsonProperty("max_price")
    private BigDecimal maxPrice;

}
