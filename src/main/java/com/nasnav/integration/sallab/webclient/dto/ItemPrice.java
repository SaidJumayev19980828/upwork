package com.nasnav.integration.sallab.webclient.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemPrice {

    private String itemNumber;
    private BigDecimal price;
    private BigDecimal discountRate;
    private BigDecimal discountPerc;
    private Integer addingFlag;
    private Integer addingValue;
    private BigDecimal netPrice;
}
