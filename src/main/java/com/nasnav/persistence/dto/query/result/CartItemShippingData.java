package com.nasnav.persistence.dto.query.result;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CartItemShippingData {
	private Long stockId;
	private Long shopId;
	private Long shopAddressId;
	private BigDecimal price;
	private BigDecimal discount;
	private Integer quantity;
}
