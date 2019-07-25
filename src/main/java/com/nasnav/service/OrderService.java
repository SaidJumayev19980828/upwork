package com.nasnav.service;

import com.nasnav.dto.OrderJsonDto;
import com.nasnav.dto.OrderRepresentationObject;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.response.OrderResponse;

import java.math.BigDecimal;
import java.util.List;


public interface OrderService {

	public class OrderValue {
		public BigDecimal amount;
		public TransactionCurrency currency;
	}

	public OrderResponse updateOrder(OrderJsonDto orderJson,Long userId);

	public OrderResponse getOrderInfo(Long orderId);

	public OrderValue getOrderValue(OrdersEntity orderEntity);

	public List<OrderRepresentationObject> getOrdersList(Long userId, Long storeId, String status);
}
