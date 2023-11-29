package com.nasnav.dto;

import com.nasnav.enumerations.TransactionCurrency;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductStockDTO {
   private Long id;
   private Integer quantity;
    private BigDecimal price ;
    private TransactionCurrency currency;
    private BigDecimal discount;
    private Long variantId;
}
