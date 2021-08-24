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
import com.nasnav.persistence.MetaOrderEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.PaymentEntity;
import com.nasnav.persistence.dto.query.result.CartCheckoutData;
import com.nasnav.request.OrderSearchParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


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

	String BILL_EMAIL_SUBJECT = "Your Order has been Created at %s";
	String ORDER_REJECT_SUBJECT = "Sorry! Your Order has been rejected from %s";

	void updateExistingOrder(OrderJsonDto orderJson);

	public DetailedOrderRepObject getOrderInfo(Long orderId, Integer detailsLevel)  throws BusinessException;

	public List<DetailedOrderRepObject> getOrdersList(OrderSearchParam params) throws BusinessException;

	void finalizeOrder(Long orderId) throws BusinessException;

	public void setOrderAsPaid(PaymentEntity payment, OrdersEntity order);

	public OrderConfirmResponseDTO confrimOrder(Long orderId);

	public ArrayList<OrdersEntity> getOrdersForMetaOrder(Long metaOrderId);

	Order getMetaOrder(Long id);
	List<MetaOrderBasicInfo> getMetaOrderList();

	public OrderValue getMetaOrderTotalValue(long metaOrderId);

	public void rejectOrder(OrderRejectDTO dto);

	public void cancelOrder(Long metaOrderId);

	List<CartCheckoutData> createCheckoutData(Cart cart);

	OrdersEntity updateOrderStatus(OrdersEntity orderEntity, OrderStatus newStatus);

	MetaOrderEntity createMetaOrder(CartCheckoutDTO dto);

	Order createOrder(CartCheckoutDTO dto);

	String trackOrder(Long orderId);
}
