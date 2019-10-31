package com.nasnav.integration.microsoftdynamics.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class Product {

    private String Ax_ID;

    @JsonProperty("ItemDesc")
    private String itemDescription;
    private String itemGroup;
    private String name;
    private BigDecimal originalPrice;
    //Qty: []

    @JsonProperty("SKU")
    private String sku;
}
