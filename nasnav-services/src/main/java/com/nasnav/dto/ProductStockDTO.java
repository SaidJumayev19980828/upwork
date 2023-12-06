package com.nasnav.dto;

import com.nasnav.enumerations.TransactionCurrency;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductStockDTO {
   private Long id;
   private Integer quantity;
    private BigDecimal price ;
    private TransactionCurrency currency;
    private BigDecimal discount;
    private Long variant_id;
}
