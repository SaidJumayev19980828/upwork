package com.nasnav.integration.events.data;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class OrderData {
	private Long orderId;
	private Long organizationId;
	private Long userId;
	private Long shopId;
	private String address;
	private BigDecimal totalValue;
	private List<OrderItemData> items;
	
}
