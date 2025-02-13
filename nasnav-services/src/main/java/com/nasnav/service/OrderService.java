package com.nasnav.service;

import com.nasnav.dto.*;
import com.nasnav.dto.request.OrderRejectDTO;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.response.OrderConfirmResponseDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.*;
import com.nasnav.persistence.dto.query.result.CartCheckoutData;
import com.nasnav.request.OrderSearchParam;
import com.nasnav.response.OrdersListResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public interface OrderService {



	class OrderValue {
		public  BigDecimal amount;
		public  TransactionCurrency currency;

		public String toString() {
			if (amount == null) {
				return "NULL";
			}
			return amount.toString() + " " + currency;
		}

	}

	String BILL_EMAIL_SUBJECT = "Your Order has been Created at %s";
	String ORDER_REJECT_SUBJECT = " %s - Order  %s Rejection.";

	void updateExistingOrder(OrderJsonDto orderJson);

	DetailedOrderRepObject getOrderInfo(Long orderId, Integer detailsLevel);

	OrdersListResponse getOrdersInfoByUserEmailWithinWeek(String userEmail, Integer detailsLevel);

	void finalizeOrder(Long orderId);

	void finalizeYeshteryMetaOrder(MetaOrderEntity metaOrder, Set<OrdersEntity> subOrders);

	void setOrderAsPaid(PaymentEntity payment, OrdersEntity order);

	OrderConfirmResponseDTO confirmOrder(Long orderId, String pinCode);

	ArrayList<OrdersEntity> getOrdersForMetaOrder(Long metaOrderId);

	Order getMetaOrder(Long id, boolean yeshteryMetaorder);

	Order getYeshteryMetaOrder(Long orderId, boolean yeshteryMetaorder);

	List<MetaOrderBasicInfo> getMetaOrderList();

	OrderValue getMetaOrderTotalValue(long metaOrderId);

	void rejectOrder(OrderRejectDTO dto);

	void cancelOrder(Long metaOrderId, boolean yeshteryMetaOrder);

	List<CartCheckoutData> createCheckoutData(Cart cart);

	OrdersEntity updateOrderStatus(OrdersEntity orderEntity, OrderStatus newStatus);

	MetaOrderEntity createMetaOrder(CartCheckoutDTO dto, OrganizationEntity org, BaseUserEntity user);

	Order createOrder(CartCheckoutDTO dto);

	Order createOrder(CartCheckoutDTO dto, UserEntity user);


	Integer countOrdersByUserId(Long userId);

	String trackOrder(Long orderId);

	DetailedOrderRepObject getYeshteryOrderInfo(Long orderId, Integer detailsLevel)  throws BusinessException;

	OrdersListResponse getYeshteryOrdersList(OrderSearchParam params) throws BusinessException;

	OrdersListResponse getOrdersList(OrderSearchParam params) throws BusinessException;

	OrdersListResponse getOrdersListPageable(OrderSearchParam params) throws BusinessException;

	OrdersListResponse getAllOrdersList(OrderSearchParam params);


	OrdersFiltersResponse getOrdersAvailableFilters (OrderSearchParam orderSearchParam) throws BusinessException;

	List<MetaOrderBasicInfo> getYeshteryMetaOrderList();

	MetaOrderEntity createYeshteryMetaOrder(CartCheckoutDTO dto);

	Order createYeshteryOrder(CartCheckoutDTO dto);

	PaymentEntity validateOrderForPaymentCoD(Long metaOrderId) throws BusinessException;
}
