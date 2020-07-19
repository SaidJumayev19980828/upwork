package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class BasketItem {

    @JsonProperty("product_id")
    private Long productId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("p_name")
    private String pname;
    @JsonProperty("stock_id")
    private Long stockId;
    @JsonProperty("brand_id")
    private Long brandId;
    @JsonProperty("variant_features")
    private Map<String, String> variantFeatures;
    @JsonProperty("quantity")
    private Integer quantity;
    @JsonProperty("total_price")
    private BigDecimal totalPrice;
    @JsonProperty("unit")
    private String unit;
    @JsonProperty("thumb")
    private String thumb;

    @JsonIgnore
    private String currency;
}
