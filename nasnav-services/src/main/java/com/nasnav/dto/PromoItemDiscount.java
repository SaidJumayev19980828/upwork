package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PromoItemDiscount {
    private PromoItemDto item;
    @JsonIgnore
    private BigDecimal discount;

    public PromoItemDiscount(PromoItemDto item, BigDecimal discount) {
        this.item = item;
        this.discount = discount;
    }
}
