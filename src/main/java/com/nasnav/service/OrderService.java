package com.nasnav.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.nasnav.dto.DetailedOrderRepObject;
import com.nasnav.dto.MetaOrderBasicInfo;
import com.nasnav.dto.OrderJsonDto;
import com.nasnav.dto.ReturnRequestSearchParams;
import com.nasnav.dto.request.OrderRejectDTO;
import com.nasnav.dto.request.ReturnRequestRejectDTO;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.order.returned.ReceivedItemsDTO;
import com.nasnav.dto.request.order.returned.ReturnRequestItemsDTO;
import com.nasnav.dto.response.OrderConfrimResponseDTO;
import com.nasnav.dto.response.ReturnRequestDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.PaymentEntity;
import com.nasnav.persistence.dto.query.result.CartCheckoutData;
import com.nasnav.request.OrderSearchParam;
import com.nasnav.response.OrderResponse;
import com.nasnav.response.ReturnRequestsResponse;
import com.nasnav.service.model.cart.ShopFulfillingCart;


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

	String BILL_EMAIL_SUBJECT = "Your Order has been Created!";
	String ORDER_REJECT_SUBJECT = "Sorry! Your Order has been rejected!";
	String ORDER_RETURN_REJECT_SUBJECT = "Sorry! Your Order return has been rejected!";
	String ORDER_RETURN_CONFIRM_SUBJECT = "Your Order return has been confirmed!";


	public OrderResponse createNewOrder(OrderJsonDto orderJson) throws BusinessException;

	public OrderResponse updateExistingOrder(OrderJsonDto orderJson) throws BusinessException;

	public DetailedOrderRepObject getOrderInfo(Long orderId, Integer detailsLevel)  throws BusinessException;

	public OrderValue getOrderValue(OrdersEntity orderEntity);

	public List<DetailedOrderRepObject> getOrdersList(OrderSearchParam params) throws BusinessException;

	public DetailedOrderRepObject getCurrentOrder(Integer detailsLevel) throws BusinessException;

	public void deleteOrders(List<Long> orderIds) throws BusinessException;

	public void deleteCurrentOrders();
	
	void finalizeOrder(Long orderId) throws BusinessException;

	public void setOrderAsPaid(PaymentEntity payment, OrdersEntity order);
	
	public Cart getCart();

	public Cart addCartItem(CartItem item);

	public Cart deleteCartItem(Long itemId);

	public OrderConfrimResponseDTO confrimOrder(Long orderId);
	public Order checkoutCart(CartCheckoutDTO dto) throws BusinessException, IOException;
	public ArrayList<OrdersEntity> getOrdersForMetaOrder(Long metaOrderId);

	Order getMetaOrder(Long id);
	List<MetaOrderBasicInfo> getMetaOrderList();

	public OrderValue getMetaOrderTotalValue(long metaOrderId);

	List<ShopFulfillingCart> getShopsThatCanProvideCartItems();

	List<ShopFulfillingCart> getShopsThatCanProvideWholeCart();

	public void rejectOrder(OrderRejectDTO dto);

	Cart getUserCart(Long userId);

	public void cancelOrder(Long metaOrderId);

	List<CartCheckoutData> createCheckoutData(Cart cart);

	OrdersEntity updateOrderStatus(OrdersEntity orderEntity, OrderStatus newStatus);

	BigDecimal calculateCartTotal();

	ReturnRequestsResponse getOrderReturnRequests(ReturnRequestSearchParams params);
	ReturnRequestDTO getOrderReturnRequest(Long id);
	void rejectReturnRequest(ReturnRequestRejectDTO dto);

	void receiveItems(ReceivedItemsDTO returnedItems);

	Long createReturnRequest(ReturnRequestItemsDTO itemsList);

	void confirmReturnRequest(Long id);
}
