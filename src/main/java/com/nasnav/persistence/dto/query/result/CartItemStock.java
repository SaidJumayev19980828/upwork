package com.nasnav.persistence.dto.query.result;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemStock {
	private Long variantId;
	private Long stockId;
	private Long shopId;
	private Long shopCityId;
	private Integer stockQuantity;
	private BigDecimal stockPrice;
}	
