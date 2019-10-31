package com.nasnav.integration.microsoftdynamics.webclient.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesOrderItem
{
    private String item;
    private String inventSiteId;
    private String store;
    private String code;
    private BigDecimal qty;
    private BigDecimal salesPrice;
    private BigDecimal discountAmount;
    private BigDecimal netPrice;
    private BigDecimal totals;
}
