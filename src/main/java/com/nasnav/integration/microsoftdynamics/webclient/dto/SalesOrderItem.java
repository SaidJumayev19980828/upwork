package com.nasnav.integration.microsoftdynamics.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesOrderItem
{
    @JsonProperty("Item")
    private String item;
    @JsonProperty("InventSiteID")
    private String inventSiteId;
    @JsonProperty("Store")
    private String store;
    @JsonProperty("Code")
    private String code;
    @JsonProperty("Qty")
    private BigDecimal quantity;
    @JsonProperty("SalesPrice")
    private BigDecimal salesPrice;
    @JsonProperty("DiscountAmount")
    private BigDecimal discountAmount;
    @JsonProperty("NetPrice")
    private BigDecimal netPrice;
    @JsonProperty("Totals")
    private BigDecimal totals;
}
