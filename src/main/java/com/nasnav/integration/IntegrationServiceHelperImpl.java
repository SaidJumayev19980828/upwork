package com.nasnav.integration;

import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_CUSTOMER_MAPPING_FAILED;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_ORDER_MAPPING_FAILED;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_PAYMENT_MAPPING_FAILED;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_INTEGRATION_EVENT_PROCESSING_FAILED;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_INTEGRATION_EVENT_PUSH_FAILED;
import static com.nasnav.integration.enums.MappingType.ORDER;
import static com.nasnav.integration.enums.MappingType.PAYMENT;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDateTime.now;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.enums.MappingType;
import com.nasnav.integration.events.CustomerCreateEvent;
import com.nasnav.integration.events.Event;
import com.nasnav.integration.events.EventResult;
import com.nasnav.integration.events.OrderConfirmEvent;
import com.nasnav.integration.events.PaymentCreateEvent;
import com.nasnav.integration.events.data.CustomerData;
import com.nasnav.integration.events.data.OrderData;
import com.nasnav.integration.events.data.OrderItemData;
import com.nasnav.integration.events.data.PaymentData;
import com.nasnav.integration.exceptions.InvalidIntegrationEventException;
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.PaymentEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.service.SecurityService;

@Service
public class IntegrationServiceHelperImpl implements IntegrationServiceHelper {
	
	private static Logger logger = LogManager.getLogger();
	
	
	
	@Autowired
	IntegrationService integrationService;
	
	
	@Autowired
	SecurityService securityService;

	@Override
	public void pushCustomerCreationEvent(CustomerData customer, Long orgId) {
		CustomerCreateEvent event = new CustomerCreateEvent(orgId, customer, this::saveUserExternalId);		
		pushEvent(event);		
	}
	
	
	
	
	
	private void saveUserExternalId(EventResult<CustomerData, String> result) {
		CustomerData customer = result.getEventInfo().getEventData();
		Long orgId = result.getEventInfo().getOrganizationId();
		String remoteId = result.getReturnedData();
		
		try {
			integrationService.addMappedValue(orgId, MappingType.CUSTOMER, String.valueOf(customer.getId()), remoteId);
		} catch (BusinessException e) {
			logger.error( format(ERR_CUSTOMER_MAPPING_FAILED, customer.toString(), remoteId), e);
		}
	}
	
	
	
	
	
	private <E extends Event<D,R>,D,R> void handleGeneralIntegrationError(E event, Throwable error) {
		Long orgId = event.getOrganizationId();
		logger.error( format(ERR_INTEGRATION_EVENT_PROCESSING_FAILED
							, CustomerCreateEvent.class.getName()
							, event.getEventInfo().getEventData()
							, orgId)
				, error);
		
		integrationService.runGeneralErrorFallback(event, error, null);		
	}





	@Override
	public void pushOrderConfirmEvent(OrdersEntity order) {
		//TODO some errors can happen during creating the order data, we need to handle this 
		//and send an error to admins if something happened		
		OrderData orderData = createOrderData(order);	
		
		OrderConfirmEvent event = new OrderConfirmEvent(orderData.getOrganizationId(), orderData, this::saveOrderExternalId);
		
		pushEvent(event);
	}





	private <E extends Event<D,R>,D,R> void pushEvent(E event) {
		try {
			integrationService.pushIntegrationEvent(event, this::handleGeneralIntegrationError);
		} catch (InvalidIntegrationEventException e) {
			Object dat = event.getEventInfo().getEventData();
			logger.error(
					format(ERR_INTEGRATION_EVENT_PUSH_FAILED, OrderConfirmEvent.class.getName(), dat.toString(), event.getOrganizationId())
					, e);
			handleGeneralIntegrationError(event, e);
		}
	}
	
	
	
	
	private void saveOrderExternalId(EventResult<OrderData, String> result) {
		OrderData order = result.getEventInfo().getEventData();
		Long orgId = result.getEventInfo().getOrganizationId();
		String remoteId = result.getReturnedData();
		
		try {
			integrationService.addMappedValue(orgId, ORDER, String.valueOf(order.getOrderId()), remoteId);
		} catch (BusinessException e) {
			logger.error( format(ERR_ORDER_MAPPING_FAILED, order.toString(), remoteId), e);
		}
	}





	private OrderData createOrderData(OrdersEntity order) {
		OrderData data = new OrderData();
		data.setOrderId(order.getId());
		data.setAddress(order.getAddress());
		data.setShopId(order.getShopsEntity().getId());
		data.setTotalValue(order.getAmount());
		data.setUserId(order.getUserId());
		data.setItems(getOrderItems(order));
		data.setOrganizationId(order.getOrganizationEntity().getId());
		return data;
	}





	private List<OrderItemData> getOrderItems(OrdersEntity order) {
		return ofNullable(order)
				.map(OrdersEntity::getBasketsEntity)
				.orElse(emptySet())
				.stream()
				.map(this::toOrderItemData)
				.collect(Collectors.toList());
	}
	
	
	
	
	
	
	private OrderItemData toOrderItemData(BasketsEntity basketItem) {
		OrderItemData data = new OrderItemData();
		StocksEntity stock = basketItem.getStocksEntity();
		data.setItemPrice(stock.getPrice());
		data.setQuantity(basketItem.getQuantity());
		data.setVariantId(stock.getProductVariantsEntity().getId());
		return data;
	}





	@Override
	public void pushNewPaymentEvent(PaymentEntity payment){
		PaymentData data = createPaymentData(payment);		
		Long orgId = getOrgId(payment);
		
		PaymentCreateEvent event = new PaymentCreateEvent(orgId, data, this::savePaymentExternalId);
		pushEvent(event);
	}





	private Long getOrgId(PaymentEntity payment) {
		return ofNullable(payment)
				.map(PaymentEntity::getOrdersEntity)
				.map(OrdersEntity::getOrganizationEntity)
				.map(OrganizationEntity::getId)
				.orElseGet(() -> securityService.getCurrentUserOrganizationId());
	}





	private PaymentData createPaymentData(PaymentEntity payment) {
		PaymentData data = new PaymentData();
		String currency = getCurrency(payment);
		LocalDateTime executionTime = getExecutionTime(payment);
		BigDecimal amount = getAmount(payment);
		
		data.setCurrency(currency);
		data.setExcutionTime(executionTime);
		data.setId(payment.getId());
		data.setOrderId(payment.getOrdersEntity().getId());
		data.setUserId(payment.getOrdersEntity().getUserId());
		data.setValue(amount);
		
		return data;
	}





	private BigDecimal getAmount(PaymentEntity payment) {
		return ofNullable(payment)
				.map(PaymentEntity::getAmount)
				.orElse(ZERO);
	}





	private LocalDateTime getExecutionTime(PaymentEntity payment) {
		return ofNullable(payment)
				.map(PaymentEntity::getExecuted)
				.map(Date::toInstant)
				.map(instant -> instant.atZone(ZoneId.systemDefault()))
				.map(ZonedDateTime::toLocalDateTime)
				.orElse(now());
	}





	private String getCurrency(PaymentEntity payment) {
		return ofNullable(payment)
				.map(PaymentEntity::getCurrency)
				.map(Enum::name)
				.orElse("");
	}
	
	
	
	
	
	
	private void savePaymentExternalId(EventResult<PaymentData, String> result) {
		PaymentData payment = result.getEventInfo().getEventData();
		Long orgId = result.getEventInfo().getOrganizationId();
		String remoteId = result.getReturnedData();
		
		try {
			integrationService.addMappedValue(orgId, PAYMENT, String.valueOf(payment.getId()), remoteId);
		} catch (BusinessException e) {
			logger.error( format(ERR_PAYMENT_MAPPING_FAILED, payment.toString(), remoteId), e);
		}
	}
	
	
	
}
