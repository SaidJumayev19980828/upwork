package com.nasnav.persistence.dto.query.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemStock {
	private Long orgId;
	private Long variantId;
	private Long stockId;
	private Long shopId;
	private Long shopCityId;
	private Integer stockQuantity;
	private BigDecimal stockPrice;
	private BigDecimal discount;
}	
