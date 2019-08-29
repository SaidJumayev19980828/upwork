package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.math.BigDecimal;

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
