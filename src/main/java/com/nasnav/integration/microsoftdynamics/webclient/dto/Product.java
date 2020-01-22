package com.nasnav.integration.microsoftdynamics.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class Product {

    @JsonProperty("Ax_ID")
    private String axId;
    
    @JsonProperty("ItemDesc")
    private String itemDescription;
    
    @JsonProperty("Name")
    private String name;
    
    @JsonProperty("OriginalPrice")
    private BigDecimal originalPrice;
    
    @JsonProperty("Qty")
    private List<Stocks> stocks;
    
    @JsonProperty("SKU")
    private String sku;
    
    @JsonProperty("Brand")
    private String brand;
    
    @JsonProperty("Category")
    private String category;
}
