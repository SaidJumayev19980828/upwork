package com.nasnav.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class PromoItemDto {
    private Integer quantity;
    private BigDecimal price;
    private Long productId;
    private Long variantId;
    private Long stockId;
    private BigDecimal weight;
    private BigDecimal discount;
    private String unit;
    private Integer productType;
    private Long brandId;
    private String itemData;
}
