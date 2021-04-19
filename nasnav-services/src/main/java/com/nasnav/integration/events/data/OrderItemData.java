package com.nasnav.integration.events.data;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemData {
	private Long variantId;
	private BigDecimal quantity;
	private BigDecimal itemPrice;	
}
