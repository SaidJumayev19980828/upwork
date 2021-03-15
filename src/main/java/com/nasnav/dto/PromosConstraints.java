package com.nasnav.dto;

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
    private AppliedTo brands;
    private AppliedTo tags;
    private AppliedTo products;
    private Long useLimit;
    private Long useLimitPerOrder;
    private Long useLimitPerUser;
    private Integer productQuantityMin;
    private Integer productToGive;
}
