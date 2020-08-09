package com.nasnav.integration;

import static com.nasnav.commons.utils.StringUtils.nullableToString;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_CUSTOMER_MAPPING_FAILED;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_INTEGRATION_EVENT_PROCESSING_FAILED;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_INTEGRATION_EVENT_PUSH_FAILED;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_ORDER_MAPPING_FAILED;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_PAYMENT_MAPPING_FAILED;
import static com.nasnav.integration.IntegrationServiceImpl.REQUEST_TIMEOUT_SEC;
import static com.nasnav.integration.enums.MappingType.ORDER;
import static com.nasnav.integration.enums.MappingType.PAYMENT;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.time.Duration.ofSeconds;
import static java.time.LocalDateTime.now;
import static java.util.Collections.emptySet;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.dao.MetaOrderRepository;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.AddressRepObj;
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
import com.nasnav.integration.exceptions.ExternalOrderIdNotFound;
import com.nasnav.integration.exceptions.InvalidIntegrationEventException;
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.MetaOrderEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.PaymentEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.service.SecurityService;

@Service
public class IntegrationServiceAdapterImpl implements IntegrationServiceAdapter {
	
	private static final int PAYMENT_DELAY_FACTOR = 2;



	private static Logger logger = LogManager.getLogger();
	
	
	
	@Autowired
	IntegrationService integrationService;
	
	
	@Autowired
	SecurityService securityService;

	@Autowired
	UserRepository userRepository;
	
	
	@Autowired
	OrdersRepository orderRepo;
	
	@Autowired
	MetaOrderRepository metaOrderRepo;

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
			integrationService.addMappedValue(orgId, MappingType.CUSTOMER, nullableToString(customer.getId()), remoteId);
		} catch (BusinessException e) {
			logger.error( format(ERR_CUSTOMER_MAPPING_FAILED, customer.toString(), remoteId), e);
		}
	}
	
	
	
	
	
	private <E extends Event<D,R>,D,R> void generalIntegrationErrorHandler(E event, Throwable error) {
		Long orgId = event.getOrganizationId();
		logger.error( format(ERR_INTEGRATION_EVENT_PROCESSING_FAILED
							, event.getClass().getName()
							, event.getEventInfo().getEventData()
							, orgId)
				, error);
		
		integrationService.runGeneralErrorFallback(event, error, error);		
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
			integrationService.pushIntegrationEvent(event, this::generalIntegrationErrorHandler);
		} catch (InvalidIntegrationEventException e) {
			Object dat = event.getEventInfo().getEventData();
			logger.error(
					format(ERR_INTEGRATION_EVENT_PUSH_FAILED, event.getClass().getName(), dat.toString(), event.getOrganizationId())
					, e);
			generalIntegrationErrorHandler(event, e);
		}
	}
	
	
	
	private <E extends Event<D,R>,D,R> void pushPaymentEvent(E event) {
		try {
			integrationService.pushIntegrationEvent(event, this::onPaymentErrorHandler);
		} catch (InvalidIntegrationEventException e) {
			Object dat = event.getEventInfo().getEventData();
			logger.error(
					format(ERR_INTEGRATION_EVENT_PUSH_FAILED, event.getClass().getName(), dat.toString(), event.getOrganizationId())
					, e);
			generalIntegrationErrorHandler(event, e);
		}
	}
	

	
	
	
	private <E extends Event<D,R>,D,R> void onPaymentErrorHandler(E event, Throwable error) {
		if(error instanceof ExternalOrderIdNotFound) {
			integrationService.retryEvent(event, this::generalIntegrationErrorHandler, ofSeconds(PAYMENT_DELAY_FACTOR*REQUEST_TIMEOUT_SEC), 3);
		}else {
			generalIntegrationErrorHandler(event, error);
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
		if (order.getAddressEntity() != null) {
			data.setAddress((AddressRepObj) order.getAddressEntity().getRepresentation());
		}
		data.setShopId(order.getShopsEntity().getId());
		data.setTotalValue(order.getAmount());
		data.setUserId(order.getUserId());
		data.setItems(getOrderItems(order));
		data.setOrganizationId(order.getOrganizationEntity().getId());
		data.setPaymentData( createPaymentData(order.getPaymentEntity()));
		return data;
	}





	private List<OrderItemData> getOrderItems(OrdersEntity order) {
		return ofNullable(order)
				.map(OrdersEntity::getBasketsEntity)
				.orElse(emptySet())
				.stream()
				.map(this::toOrderItemData)
				.collect(toList());
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
	@Transactional(propagation = REQUIRES_NEW)
	//without new transaction, this may cause a new flush in the transaction
	//that presists the payment causing concurrency exception for hibernate
	//anyway, this should be called after the payment transaction is completed
	public void pushPaymentEvent(PaymentEntity payment){
		Optional<PaymentData> data = createPaymentData(payment);	
		if(!data.isPresent()) {
			return;
		}
		Long orgId = getOrganizationId(payment);
		
		PaymentCreateEvent event = new PaymentCreateEvent(orgId, data.get(), this::savePaymentExternalId);
		pushPaymentEvent(event);
	}





	

	private Optional<PaymentData> createPaymentData(PaymentEntity payment) {
		//in case of cash-on-delivery, payment can be null
		if(payment == null) {
			return empty();
		}
		
		PaymentData data = new PaymentData();
		String currency = getCurrency(payment);
		LocalDateTime executionTime = getExecutionTime(payment);
		BigDecimal amount = getAmount(payment);
		Long orgId = getOrganizationId(payment);
		
		data.setCurrency(currency);
		data.setExcutionTime(executionTime);
		data.setId(payment.getId());
		data.setUserId(payment.getUserId());
		data.setValue(amount);
		data.setOrganizationId(orgId);
		
		return Optional.of(data);
	}





	private Long getOrganizationId(PaymentEntity payment) {
		return ofNullable(payment)
				.map(PaymentEntity::getMetaOrderId)
				.flatMap(metaOrderRepo::findMetaOrderWithOrganizationById)
				.map(MetaOrderEntity::getOrganization)
				.map(OrganizationEntity::getId)
				.orElse(null);
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
			integrationService.addMappedValue(orgId, PAYMENT, nullableToString(payment.getId()), remoteId);
		} catch (BusinessException e) {
			logger.error( format(ERR_PAYMENT_MAPPING_FAILED, payment.toString(), remoteId), e);
		}
	}
	
	
	
}
