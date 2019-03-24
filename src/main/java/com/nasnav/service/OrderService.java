package com.nasnav.service;

import com.nasnav.persistence.OrdersEntity;
import com.nasnav.response.OrderResponse;

public interface OrderService {

	public OrderResponse updateOrder(String orderJson);
	
	public OrderResponse getOrderInfo(Long orderId);

}
