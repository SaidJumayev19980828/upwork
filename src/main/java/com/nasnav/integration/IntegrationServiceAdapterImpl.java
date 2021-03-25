package com.nasnav.integration;

import com.nasnav.commons.utils.FunctionalUtils;
import com.nasnav.dao.MetaOrderRepository;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.PaymentsRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.AddressRepObj;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.enums.MappingType;
import com.nasnav.integration.events.*;
import com.nasnav.integration.events.data.CustomerData;
import com.nasnav.integration.events.data.OrderData;
import com.nasnav.integration.events.data.OrderItemData;
import com.nasnav.integration.events.data.PaymentData;
import com.nasnav.integration.exceptions.ExternalOrderIdNotFound;
import com.nasnav.integration.exceptions.InvalidIntegrationEventException;
import com.nasnav.persistence.*;
import com.nasnav.service.SecurityService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.nasnav.commons.utils.StringUtils.nullableToString;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.*;
import static com.nasnav.integration.IntegrationServiceImpl.REQUEST_TIMEOUT_SEC;
import static com.nasnav.integration.enums.MappingType.ORDER;
import static com.nasnav.integration.enums.MappingType.PAYMENT;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.FLOOR;
import static java.time.Duration.ofSeconds;
import static java.time.LocalDateTime.now;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Service
public class IntegrationServiceAdapterImpl implements IntegrationServiceAdapter {

	private static final int PAYMENT_DELAY_FACTOR = 2;



	private static Logger logger = LogManager.getLogger();



	@Autowired
	private IntegrationService integrationService;


	@Autowired
	private SecurityService securityService;

	@Autowired
	private UserRepository userRepository;


	@Autowired
	private OrdersRepository orderRepo;

	@Autowired
	private MetaOrderRepository metaOrderRepo;

	@Autowired
	private PaymentsRepository paymentRepo;

	@Override
	@Transactional(propagation = REQUIRES_NEW)
	//without new transaction, this may cause a new flush in the transaction
	//that presists the payment causing concurrency exception for hibernate
	//anyway, this should be called after the payment transaction is completed
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



	private Mono<EventResult<OrderData, OrderData.Result>> pushSubOrderFinalizeEvent(OrdersEntity order) {
		//TODO some errors can happen during creating the order data, we need to handle this
		//and send an error to admins if something happened
		OrderData orderData = createOrderData(order);

		OrderConfirmEvent event = new OrderConfirmEvent(orderData.getOrganizationId(), orderData, this::saveOrderExternalId);

		return pushEvent(event);
	}



	private <E extends Event<D,R>,D,R> Mono<EventResult<D,R>> pushEvent(E event) {
		try {
			return integrationService.pushIntegrationEvent(event, this::generalIntegrationErrorHandler);
		} catch (InvalidIntegrationEventException e) {
			Object dat = event.getEventInfo().getEventData();
			logger.error(
					format(ERR_INTEGRATION_EVENT_PUSH_FAILED, event.getClass().getName(), dat.toString(), event.getOrganizationId())
					, e);
			generalIntegrationErrorHandler(event, e);
			return Mono.error(e);
		}
	}



	private <E extends Event<D,R>,D,R> void onPaymentErrorHandler(E event, Throwable error) {
		if(error instanceof ExternalOrderIdNotFound) {
			integrationService.retryEvent(event, this::generalIntegrationErrorHandler, ofSeconds(PAYMENT_DELAY_FACTOR*REQUEST_TIMEOUT_SEC), 3);
		}else {
			generalIntegrationErrorHandler(event, error);
		}
	}



	private void saveOrderExternalId(EventResult<OrderData, OrderData.Result> result) {
		OrderData order = result.getEventInfo().getEventData();
		Long orgId = result.getEventInfo().getOrganizationId();
		String remoteId = result.getReturnedData().getExternalId();
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



//	private Mono<EventResult<PaymentData, String>> pushPaymentEvent(PaymentEntity payment){
//		Optional<PaymentData> data = createPaymentData(payment);
//		if(!data.isPresent()) {
//			return null;
//		}
//		Long orgId = getOrganizationId(payment);
//
//		PaymentCreateEvent event = new PaymentCreateEvent(orgId, data.get(), this::savePaymentExternalId);
//		return pushEvent(event);
//	}



	@Override
	@Transactional(propagation = REQUIRES_NEW)
	//without new transaction, this may cause a new flush in the transaction
	//that presists the payment causing concurrency exception for hibernate
	//anyway, this should be called after the payment transaction is completed
	public void pushMetaOrderFinalizeEvent(MetaOrderEntity order) {
		Optional<PaymentEntity> payment = paymentRepo.findByMetaOrderId(order.getId());
		Flux.fromIterable(orderRepo.findInDetailsByMetaOrderId(order.getId()))
				.flatMap(this::pushSubOrderFinalizeEvent)
				.map(EventResult::getReturnedData)
				.collectList()
				.map(this::toOrderIdMap)
				.map(mapping -> new PaymentAndOrderIdMapping(payment, mapping))
				.filter(PaymentAndOrderIdMapping::isPaymentPresent)
				.flatMap(pay -> pushPaymentEvent(pay, order))
				.subscribe(this::saveMetaOrderPaymentExternalId);
	}



	public Mono<List<EventResult<PaymentData, String>>> pushPaymentEvent(PaymentAndOrderIdMapping payment, MetaOrderEntity metaOrder){
		Long orgId = getOrganizationId(metaOrder);
		return createPaymentPerSubOrder(payment, metaOrder)
				.map(subPayment -> new PaymentCreateEvent(orgId, subPayment, FunctionalUtils::doNothing))
				.flatMap(this::pushEvent)
				.collectList()
				.timeout(Duration.ofMinutes(2));
	}



	private Flux<PaymentData> createPaymentPerSubOrder(PaymentAndOrderIdMapping payment, MetaOrderEntity metaOrder) {
		return metaOrder
				.getSubOrders()
				.stream()
				.map(subOrder -> createPaymentData(payment, subOrder, metaOrder))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(
						collectingAndThen(
								toList(),
								subPayments -> {
									addChangeToFirstPayment(subPayments, payment);
									return Flux.fromIterable(subPayments);
								}));
	}



	private void saveMetaOrderPaymentExternalId(List<EventResult<PaymentData, String>> eventResults) {
		String remoteId = getSubPaymentsRemoteIds(eventResults);
		Optional<EventInfo<PaymentData>> eventInfo =
				eventResults
						.stream()
						.map(EventResult::getEventInfo)
						.findFirst();
		Long paymentLocalId =
				eventInfo
						.map(EventInfo::getEventData)
						.map(PaymentData::getId)
						.orElse(null);
		Optional<Long> orgId = eventInfo.map(EventInfo::getOrganizationId);
		try {
			integrationService.addMappedValue(orgId.get(), PAYMENT, nullableToString(paymentLocalId), remoteId);
		} catch (Exception e) {
			logger.error( format(ERR_PAYMENT_MAPPING_FAILED, paymentLocalId, remoteId), e);
		}
	}



	private String getSubPaymentsRemoteIds(List<EventResult<PaymentData, String>> eventResults) {
		return eventResults
				.stream()
				.map(EventResult::getReturnedData)
				.collect(collectingAndThen(toList(), JSONArray::new))
				.toString();
	}



	private void addChangeToFirstPayment(List<PaymentData> subPayments, PaymentAndOrderIdMapping payment) {
		BigDecimal totalPaymentAmount = getAmount(payment);
		BigDecimal subPaymentsTotal = subPayments.stream().map(PaymentData::getValue).reduce(ZERO, BigDecimal::add);
		BigDecimal change = totalPaymentAmount.subtract(subPaymentsTotal);
		subPayments.stream().peek(subPay -> addChange(subPay, change)).findFirst();
	}



	private void addChange(PaymentData subPay, BigDecimal change) {
		BigDecimal newVal = subPay.getValue().add(change);
		subPay.setValue(newVal);
	}



	private Optional<PaymentData> createPaymentData(PaymentAndOrderIdMapping paymentAndMapping, OrdersEntity subOrder, MetaOrderEntity metaOrder){
		Long orgId = getOrganizationId(metaOrder);
		BigDecimal amount = getSubOrderPaymentAmount(paymentAndMapping, subOrder, metaOrder);
		Long subOrderId = subOrder.getId();
		String externalOrderId = paymentAndMapping.getOrderIdMapping().get(subOrderId);
		//in case of cash-on-delivery, payment can be null
		return paymentAndMapping
				.getPayment()
				.map(pay -> createSubOrderPayment(pay, orgId, subOrderId, amount, externalOrderId));
	}



	private BigDecimal getSubOrderPaymentAmount(PaymentAndOrderIdMapping payment, OrdersEntity subOrder, MetaOrderEntity metaOrder) {
		BigDecimal totalPaymentAmount = payment.getPayment().map(PaymentEntity::getAmount).orElse(ZERO);
		if(totalPaymentAmount.compareTo(metaOrder.getGrandTotal()) == 0){
			//if payment equals the meta-order total, then there is not need to do division and introduce
			//approximation errors
			return subOrder.getTotal();
		}
		BigDecimal proportion = subOrder.getTotal().divide(metaOrder.getGrandTotal(),2 , FLOOR);
		return proportion.multiply(totalPaymentAmount).setScale(2 , FLOOR);
	}



	private PaymentData createSubOrderPayment(PaymentEntity payment, Long orgId, Long orderId, BigDecimal amount, String externalOrderId) {
		PaymentData data = new PaymentData();
		String currency = getCurrency(payment);
		LocalDateTime executionTime = getExecutionTime(payment);

		data.setCurrency(currency);
		data.setExecutionTime(executionTime);
		data.setId(payment.getId());
		data.setUserId(payment.getUserId());
		data.setValue(amount);
		data.setOrganizationId(orgId);
		data.setOrderId(orderId);
		data.setExternalOrderId(externalOrderId);
		return data;
	}



	private Map<Long,String> toOrderIdMap(List<OrderData.Result> results) {
		return results
				.stream()
				.collect( toMap(OrderData.Result::getSubOrderId, OrderData.Result::getExternalId, FunctionalUtils::getFirst));
	}



	private  Long getOrganizationId(MetaOrderEntity metaOrder) {
		return ofNullable(metaOrder)
				.map(MetaOrderEntity::getOrganization)
				.map(OrganizationEntity::getId)
				.orElse(null);
	}



	private BigDecimal getAmount(PaymentAndOrderIdMapping payment) {
		return payment
				.getPayment()
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
			//if the meta-order has several sub-orders , a single payment can be mapped to several external payments
			//one for each sub-order
			String remoteValue =
					ofNullable(payment)
					.map(PaymentData::getId)
					.flatMap(id -> getExistingExternalIds(id, orgId, remoteId))
					.orElseGet(() -> createJsonArray(remoteId));
			integrationService.addMappedValue(orgId, PAYMENT, nullableToString(payment.getId()), remoteValue);
		} catch (Throwable e) {
			logger.error( format(ERR_PAYMENT_MAPPING_FAILED, payment.toString(), remoteId), e);
		}
	}



	private Optional<String> getExistingExternalIds(Long id, Long orgId, String paymentExternalId) {
		return ofNullable(integrationService.getRemoteMappedValue(orgId, PAYMENT, id.toString()))
				.map(JSONArray::new)
				.map(externalOrderIds -> externalOrderIds.put(paymentExternalId))
				.map(JSONArray::toString);
	}



	private String createJsonArray(String element) {
		return createJsonArray(new JSONArray(), element).toString();
	}



	private JSONArray createJsonArray(JSONArray array, String element) {
		return array.put(element);
	}

}



@Data
@AllArgsConstructor
class PaymentAndOrderIdMapping{
	private Optional<PaymentEntity> payment;
	private Map<Long,String> orderIdMapping;

	public Boolean isPaymentPresent(){
		return ofNullable(payment).map(Optional::isPresent).orElse(false);
	}
}