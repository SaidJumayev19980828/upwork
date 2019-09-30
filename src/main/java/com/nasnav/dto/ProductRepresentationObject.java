package com.nasnav.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@EqualsAndHashCode(callSuper=true)
public class ProductRepresentationObject extends ProductBaseInfo{
    
    private BigDecimal price;
    private Boolean available;
    private Long categoryId;
    private Long brandId;
    private String barcode;
    private BigDecimal discount;
    private int currency;
    private Long stockId;
}
