package com.nasnav.integration.microsoftdynamics;

import static com.nasnav.commons.utils.StringUtils.nullableToString;
import static com.nasnav.integration.enums.MappingType.CUSTOMER;
import static com.nasnav.integration.enums.MappingType.PRODUCT_VARIANT;
import static com.nasnav.integration.enums.MappingType.SHOP;
import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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
		OrderData order = event.getEventData();
		SalesOrder requestData = createSalesOrderData(order);
		return getWebClient(order.getOrganizationId())
				.createSalesOrder(requestData)
				.doOnSuccess(this::throwExceptionIfNotOk)
				.flatMap(res -> res.bodyToMono(String.class));
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
