package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class BasketItem {

    @JsonProperty("stock_id")
    private Long stockId;
    @JsonProperty("quantity")
    private Long quantity;
    private String unit;

}
