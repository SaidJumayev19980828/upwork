package com.nasnav.integration.events.data;

import static java.util.Optional.empty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
	private Optional<PaymentData> paymentData;	//in case of cash-on-delivery, payment can be null
	
	public OrderData() {
		items = new ArrayList<>();
		paymentData = empty();
	}
}
