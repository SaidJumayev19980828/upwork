package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BasketItemDTO {

    @JsonProperty(value = "stock_id")
    @Schema(name = "stock_id", example = "123")
    private Long stockId;

    @Schema(name = "quantity", example = "123")
    private Integer quantity;

    @Schema(name = "unit", example = "kg")
    private String unit;
}
