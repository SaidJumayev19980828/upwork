package com.nasnav.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductAddonsDTO {
	private Long productId;
	private Long addonId;
	 private String name;
	 private Integer type;
	 private Integer quantity;
	 private Long addonStockId;
	 private BigDecimal price;

}
