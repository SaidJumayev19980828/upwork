package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@EqualsAndHashCode(callSuper=false)
public class PromosConstraints {
    private BigDecimal cartAmountMin;
    private Long cartQuantityMin;
    private BigDecimal discountValueMax;
    private BigDecimal percentage;
    private BigDecimal amount;
    @JsonProperty("applied_to_brands")
    private Set<Long> brands;
    @JsonProperty("applied_to_tags")
    private Set<Long> tags;
    @JsonProperty("applied_to_products")
    private Set<Long> products;
    private Long useLimit;
    private Long useLimitPerOrder;
    private Long useLimitPerUser;
    private Integer productQuantityMin;
    private Integer productToGive;
}
