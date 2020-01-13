package com.nasnav.integration.microsoftdynamics;

import static com.nasnav.integration.enums.MappingType.ORDER;
import static java.util.Optional.ofNullable;

import java.util.Arrays;
import java.util.List;

import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.EventInfo;
import com.nasnav.integration.events.PaymentCreateEvent;
import com.nasnav.integration.events.data.PaymentData;
import com.nasnav.integration.microsoftdynamics.webclient.dto.Payment;
import com.nasnav.integration.microsoftdynamics.webclient.dto.PaymentDetails;

import reactor.core.publisher.Mono;

public class PaymentCreateEventListener extends AbstractMSDynamicsEventListener<PaymentCreateEvent, PaymentData, String> {

	private static final String PAYMENT_METHOD = "Credit_CHE";





	public PaymentCreateEventListener(IntegrationService integrationService) {
		super(integrationService);
	}
	
	
	
	

	@Override
	protected Mono<String> handleEventAsync(EventInfo<PaymentData> event) {
		
		Payment requestData = createPaymentCreateRequest(event);
		return getWebClient(event.getOrganizationId())
				.createPayment(requestData)
				.doOnSuccess(this::throwExceptionIfNotOk)
				.flatMap(res -> res.bodyToMono(String.class));
	}

	
	
	
	
	private Payment createPaymentCreateRequest(EventInfo<PaymentData> event) {
		Payment payment = new Payment();
		
		PaymentData data = event.getEventData();
		String orderId = getOrderId(data);		
		String salesId = integrationService.getRemoteMappedValue(event.getOrganizationId(), ORDER, orderId);
		
		List<PaymentDetails> paymentDetails = createPaymentDetails(event, salesId);
		payment.setPaymentDetails(paymentDetails);
		payment.setSalesId(salesId);
		
		return payment;
	}





	private String getOrderId(PaymentData data) {
		String paymentId = ofNullable(data)
							.map(PaymentData::getOrderId)
							.map(id -> id.toString())
							.orElse(null);
		return paymentId;
	}





	private List<PaymentDetails> createPaymentDetails(EventInfo<PaymentData> event, String salesId) {
		PaymentData data = event.getEventData();
		
		PaymentDetails details = new PaymentDetails();
		details.setAmount(data.getValue());
		details.setSalesId(salesId);
		details.setPaymentMethod(PAYMENT_METHOD);
		
		return Arrays.asList(details);
	}





	@Override
	protected PaymentCreateEvent handleError(PaymentCreateEvent event, Throwable t) {
		// TODO Auto-generated method stub
		return null;
	}

}
