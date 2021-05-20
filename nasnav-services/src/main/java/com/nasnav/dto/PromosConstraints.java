package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@EqualsAndHashCode(callSuper=false)
public class PromosConstraints {
    private BigDecimal cartAmountMin;
    private Long cartQuantityMin;
    private BigDecimal discountMaxValue;
    private BigDecimal percentage;
    private BigDecimal amount;
    @JsonProperty("applied_to_brands")
    private AppliedTo brands;
    @JsonProperty("applied_to_tags")
    private AppliedTo tags;
    @JsonProperty("applied_to_products")
    private AppliedTo products;
    private Long useLimit;
    private Long useLimitPerOrder;
    private Long useLimitPerUser;
    private Integer productQuantityMin;
    private Integer productToGive;
}
