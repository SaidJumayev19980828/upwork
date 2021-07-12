package com.nasnav.integration.events.data;

import com.nasnav.dto.AddressRepObj;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderData {
	private Long orderId;
	private Long organizationId;
	private Long userId;
	private Long shopId;
	private AddressRepObj address;
	private BigDecimal totalValue;
	private List<OrderItemData> items;

	public OrderData() {
		items = new ArrayList<>();
	}


	@Data
	@AllArgsConstructor
	public static class Result{
		private Long subOrderId;
		private String externalId;
	}
}
