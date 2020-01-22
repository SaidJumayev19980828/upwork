package com.nasnav.integration.events.data;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderItemData {
	private Long variantId;
	private BigDecimal quantity;
	private BigDecimal itemPrice;	
}
