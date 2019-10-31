package com.nasnav.integration.microsoftdynamics.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Product {

    @JsonProperty("Ax_ID")
    private String axId;
    @JsonProperty("ItemDesc")
    private String itemDescription;
    private String itemGroup;
    private String name;
    private BigDecimal originalPrice;
    @JsonProperty("Qty")
    private List<Stocks> stocks;
    @JsonProperty("SKU")
    private String sku;
}
