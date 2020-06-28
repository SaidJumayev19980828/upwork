package com.nasnav.service;

import java.math.BigDecimal;
import java.util.List;

import com.nasnav.dto.DetailedOrderRepObject;
import com.nasnav.dto.OrderJsonDto;
import com.nasnav.dto.response.OrderConfrimResponseDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.PaymentEntity;
import com.nasnav.request.OrderSearchParam;
import com.nasnav.response.OrderResponse;


public interface OrderService {

	public class OrderValue {
		public BigDecimal amount;
		public TransactionCurrency currency;

		public String toString() {
			if (amount == null) {
				return "NULL";
			}
			return amount.toString() + " " + currency;
		}

	}

	public OrderResponse createNewOrder(OrderJsonDto orderJson) throws BusinessException;

	public OrderResponse updateExistingOrder(OrderJsonDto orderJson) throws BusinessException;

	public DetailedOrderRepObject getOrderInfo(Long orderId, Integer detailsLevel)  throws BusinessException;

	public OrderValue getOrderValue(OrdersEntity orderEntity);

	public List<DetailedOrderRepObject> getOrdersList(OrderSearchParam params) throws BusinessException;

	public DetailedOrderRepObject getCurrentOrder(Integer detailsLevel) throws BusinessException;

	public void deleteOrders(List<Long> orderIds) throws BusinessException;

	public void deleteCurrentOrders();
	
	public void validateOrdersForCheckOut(List<OrdersEntity> orders); 
	
	public OrdersEntity checkoutOrder(OrdersEntity order);

	OrdersEntity checkoutOrder(Long orderId) throws BusinessException;

	void validateOrderIdsForCheckOut(List<Long> orderIds);

	public void setOrderAsPaid(PaymentEntity payment, OrdersEntity order);
	
	public Cart getCart();

	public Cart addCartItem(CartItem item);

	public Cart deleteCartItem(Long itemId);

	public OrderConfrimResponseDTO confrimOrder(Long orderId);
}
