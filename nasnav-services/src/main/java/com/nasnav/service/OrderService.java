package com.nasnav.service;

import com.nasnav.dto.DetailedOrderRepObject;
import com.nasnav.dto.MetaOrderBasicInfo;
import com.nasnav.dto.OrderJsonDto;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


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
	String ORDER_REJECT_SUBJECT = "Sorry! Your Order has been rejected from %s";

	void updateExistingOrder(OrderJsonDto orderJson);

	 DetailedOrderRepObject getOrderInfo(Long orderId, Integer detailsLevel)  throws BusinessException;

	 List<DetailedOrderRepObject> getOrdersList(OrderSearchParam params) throws BusinessException;

	void finalizeOrder(Long orderId) throws BusinessException;

	 void setOrderAsPaid(PaymentEntity payment, OrdersEntity order);

	 OrderConfirmResponseDTO confirmOrder(Long orderId, String pinCode, BigDecimal pointsAmount);

	 ArrayList<OrdersEntity> getOrdersForMetaOrder(Long metaOrderId);

	Order getMetaOrder(Long id, boolean yeshteryMetaorder);
	Order getYeshteryMetaOrder(Long orderId, boolean yeshteryMetaorder);
	List<MetaOrderBasicInfo> getMetaOrderList();

	 OrderValue getMetaOrderTotalValue(long metaOrderId);

	 void rejectOrder(OrderRejectDTO dto);

	 void cancelOrder(Long metaOrderId);

	List<CartCheckoutData> createCheckoutData(Cart cart);

	OrdersEntity updateOrderStatus(OrdersEntity orderEntity, OrderStatus newStatus);

	MetaOrderEntity createMetaOrder(CartCheckoutDTO dto, OrganizationEntity org, BaseUserEntity user);

	Order createOrder(CartCheckoutDTO dto);

	Integer countOrdersByUserId(Long userId);
	String trackOrder(Long orderId);
	void updateExistingYeshteryOrder(OrderJsonDto orderJson);

	DetailedOrderRepObject getYeshteryOrderInfo(Long orderId, Integer detailsLevel)  throws BusinessException;

	List<DetailedOrderRepObject> getYeshteryOrdersList(OrderSearchParam params) throws BusinessException;
	List<MetaOrderBasicInfo> getYeshteryMetaOrderList();
	void cancelYeshteryOrder(Long metaOrderId);
	MetaOrderEntity createYeshteryMetaOrder(CartCheckoutDTO dto);
	Order createYeshteryOrder(CartCheckoutDTO dto);
}
