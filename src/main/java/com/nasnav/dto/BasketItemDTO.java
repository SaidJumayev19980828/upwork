package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BasketItemDTO {

    @JsonProperty(value = "stock_id")
    @ApiModelProperty(name = "stock_id", example = "123")
    private Long stockId;

    @ApiModelProperty(name = "quantity", example = "123")
    private Integer quantity;

    @ApiModelProperty(name = "unit", example = "kg")
    private String unit;
}
