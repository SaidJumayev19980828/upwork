package com.nasnav.integration.microsoftdynamics;

import static com.nasnav.commons.utils.StringUtils.nullableToString;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_ORDER_ALREADY_HAS_EXT_ID;
import static com.nasnav.integration.enums.MappingType.CUSTOMER;
import static com.nasnav.integration.enums.MappingType.ORDER;
import static com.nasnav.integration.enums.MappingType.PRODUCT_VARIANT;
import static com.nasnav.integration.enums.MappingType.SHOP;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.util.logging.Level.SEVERE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.EventInfo;
import com.nasnav.integration.events.OrderConfirmEvent;
import com.nasnav.integration.events.data.OrderData;
import com.nasnav.integration.events.data.OrderItemData;
import com.nasnav.integration.microsoftdynamics.webclient.dto.SalesOrder;
import com.nasnav.integration.microsoftdynamics.webclient.dto.SalesOrderItem;

import reactor.core.publisher.Mono;

public class OrderConfirmEventListener extends AbstractMSDynamicsEventListener<OrderConfirmEvent, OrderData, String> {

	public OrderConfirmEventListener(IntegrationService integrationService) {
		super(integrationService);
	}

	
	
	
	@Override
	protected Mono<String> handleEventAsync(EventInfo<OrderData> event) {
		//TODO: this validation should be generalized to all payment events and it should filter invalid events 
		//before pushing them to the integration module , in the integration service.
		//we can't add this in IntegrationHelper because it runs by JPA entity listeners, and the listeners are not
		//managed by spring, which means we may not be able to control transactions, and errors are thrown if we try to 
		//read from the database.
		validateOrderEvent(event);
		
		
		OrderData order = event.getEventData();
		SalesOrder requestData = createSalesOrderData(order);
		return getWebClient(order.getOrganizationId())
				.createSalesOrder(requestData)
				.flatMap(this::throwExceptionIfNotOk)
				.flatMap(res -> res.bodyToMono(String.class))
				.map(paymentId -> paymentId.replace("\"", ""));
	}

	
	
	
	
	
	private void validateOrderEvent(EventInfo<OrderData> event) {
		OrderData order = event.getEventData();
		Long orgId = event.getOrganizationId();
		String localId = nullableToString(order.getOrderId());
		String remoteId = integrationService.getRemoteMappedValue(orgId, ORDER, localId);
		if(remoteId != null) {
			String msg = format(ERR_ORDER_ALREADY_HAS_EXT_ID, order.toString(), remoteId, orgId);
			logger.log(SEVERE, msg);
			throw new RuntimeBusinessException(msg, "INVALID INTEGRATION EVENT", INTERNAL_SERVER_ERROR);
		}
	}
	
	
	
	
	
	
	
	private SalesOrder createSalesOrderData(OrderData data) {
		String customerExtId = getCustomerExternalId(data);		
		String storeExtId = getStoreExternalId(data);
		List<SalesOrderItem> items = getSalesOrderItems(data, storeExtId);
		BigDecimal total = data.getTotalValue();
		
		SalesOrder salesOrder = new SalesOrder();
		
		salesOrder.setCashOnDeliveryFee(ZERO);
		salesOrder.setCityCode("");
		salesOrder.setCodCode("Non");
		salesOrder.setCodFeeAmount(ZERO);
		salesOrder.setCountryId("EGY");		
		salesOrder.setInventSite("OCTOBER1");
		salesOrder.setPaymentMethod("Credit_CHE");
		salesOrder.setShippingFees(ZERO);
		salesOrder.setShippingFeesCode("Non");
		salesOrder.setTotalOrderDiscount(ZERO);
		
		salesOrder.setAddress(data.getAddress());
		salesOrder.setCustomerId(customerExtId);
		salesOrder.setItems(items);
		salesOrder.setStore(storeExtId);
		salesOrder.setSubTotal(total);
		salesOrder.setTotal(total);
		
		return salesOrder;
	}




	private String getStoreExternalId(OrderData data) {
		return integrationService
				.getRemoteMappedValue(data.getOrganizationId(), SHOP, nullableToString(data.getShopId()));
	}




	private List<SalesOrderItem> getSalesOrderItems(OrderData data, String shopExtId) {
		return data.getItems()
				.stream()
				.map(it -> toSalesOrderItem(it, shopExtId, data.getOrganizationId()))
				.collect(Collectors.toList());
	}


	
	
	
	
	private SalesOrderItem toSalesOrderItem(OrderItemData data, String storeExtId, Long orgId) {
		String itemExtId = getItemExternalId(data, orgId);
		BigDecimal total = data.getItemPrice().multiply(data.getQuantity());
		
		SalesOrderItem item = new SalesOrderItem();
		item.setCode(null);
		item.setDiscountAmount(ZERO);
		item.setInventSiteId("OCTOBER1");		
		item.setQuantity(data.getQuantity());
		item.setSalesPrice(data.getItemPrice());		
		item.setStore(storeExtId);
		item.setItem(itemExtId);
		item.setNetPrice(total);
		item.setTotals(total);
		
		return item;
	}


	
	
	
	
	private String getItemExternalId(OrderItemData data, Long orgId) {
		return integrationService.getRemoteMappedValue(orgId, PRODUCT_VARIANT, nullableToString(data.getVariantId()));
	}



	
	

	private String getCustomerExternalId(OrderData data) {
		return integrationService
				.getRemoteMappedValue(data.getOrganizationId(), CUSTOMER, nullableToString(data.getUserId()));
	}




	@Override
	protected OrderConfirmEvent handleError(OrderConfirmEvent event, Throwable t) {
		// TODO Auto-generated method stub
		return null;
	}

}
