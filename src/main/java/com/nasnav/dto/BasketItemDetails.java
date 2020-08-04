package com.nasnav.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class BasketItemDetails {

    private Long orderId;
    private Long productId;
    private String productName;
    private String productPname;
    private Long variantId;
    private Long stockId;
    private Long basketId;
    private BigDecimal quantity;
    private BigDecimal price;
    private Integer currency;
}
