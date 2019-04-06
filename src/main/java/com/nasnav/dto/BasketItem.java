package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class BasketItem {

    @JsonProperty("stock_id")
    private Long stockId;
    private Long quantity;
    private String unit;

}
