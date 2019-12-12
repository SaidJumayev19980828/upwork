package com.nasnav.service;

import com.nasnav.dto.DetailedOrderRepObject;
import com.nasnav.dto.OrderJsonDto;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.response.OrderResponse;

import java.math.BigDecimal;
import java.util.List;


public interface OrderService {

	public class OrderValue {
		public BigDecimal amount;
		public TransactionCurrency currency;
	}

	public OrderResponse handleOrder(OrderJsonDto orderJson) throws BusinessException;

	public DetailedOrderRepObject getOrderInfo(Long orderId)  throws BusinessException;

	public OrderValue getOrderValue(OrdersEntity orderEntity);

	public List<DetailedOrderRepObject> getOrdersList(String userToken, Long userId, Long storeId, Long orgId,
													  String status, boolean details) throws BusinessException;

	public DetailedOrderRepObject getCurrentOrder() throws BusinessException;

	public void deleteCurrentOrders();
}
