package com.nasnav.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PromotionCalculatorData {
    private List<PromoItemDto> items;
    private BigDecimal currentDiscount;

}
