package com.nasnav.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PromoItemDiscount {
    private PromoItemDto item;
    private BigDecimal discount;

    public PromoItemDiscount(PromoItemDto item, BigDecimal discount) {
        this.item = item;
        this.discount = discount;
    }
}
