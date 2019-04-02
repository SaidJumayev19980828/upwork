package com.nasnav.service;

import com.nasnav.dto.OrderJsonDto;
import com.nasnav.response.OrderResponse;

public interface OrderService {

	public OrderResponse updateOrder(OrderJsonDto orderJson,Long userId);

	public OrderResponse getOrderInfo(Long orderId);
	
}
