package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;


@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ReturnedBasketItem {
    private Long orderItemId;
    private BigDecimal receivedQuantity;
}
