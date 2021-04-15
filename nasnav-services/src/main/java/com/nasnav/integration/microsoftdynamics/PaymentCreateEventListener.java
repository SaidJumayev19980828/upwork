package com.nasnav.integration.microsoftdynamics;

import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.EventInfo;
import com.nasnav.integration.events.PaymentCreateEvent;
import com.nasnav.integration.events.data.PaymentData;
import com.nasnav.integration.exceptions.ExternalOrderIdNotFound;
import com.nasnav.integration.microsoftdynamics.webclient.dto.Payment;
import com.nasnav.integration.microsoftdynamics.webclient.dto.PaymentDetails;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.logging.Level;

import static com.nasnav.commons.utils.StringUtils.nullableToString;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_PAYMENT_ALREADY_HAS_EXT_ID;
import static com.nasnav.integration.enums.MappingType.PAYMENT;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

public class PaymentCreateEventListener extends AbstractMSDynamicsEventListener<PaymentCreateEvent, PaymentData, String> {

	private static final String PAYMENT_METHOD = "Credit_CHE";

	public  PaymentCreateEventListener(IntegrationService integrationService) {
		super(integrationService);
	}
	

	@Override
	protected Mono<String> handleEventAsync(EventInfo<PaymentData> event) {
		//TODO: this validation should be generalized to all payment events and it should filter invalid events 
		//before pushing them to the integration module , in the integration service.
		//we can't add this in IntegrationHelper because it runs by JPA entity listeners, and the listeners are not
		//managed by spring, which means we may not be able to control transactions, and errors are thrown if we try to 
		//read from the database.
		validatePaymentEvent(event);
		
		return sendPaymentRequest(event.getEventData());
	}





	private void validatePaymentEvent(EventInfo<PaymentData> event) {
		PaymentData payment = event.getEventData();
		Long orgId = (event.getOrganizationId());
		String localId = nullableToString(payment.getId());
		String remoteId = integrationService.getRemoteMappedValue(orgId, PAYMENT, localId);
		if(remoteId != null) {
			String msg = format(ERR_PAYMENT_ALREADY_HAS_EXT_ID, payment.toString(), remoteId, orgId);
			logger.log(Level.SEVERE, msg);
			throw new RuntimeBusinessException(msg, "INVALID INTEGRATION EVENT", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	private Mono<String> sendPaymentRequest(PaymentData payment) {
		Long orgId = payment.getOrganizationId();
		return Mono
				.justOrEmpty(payment)
				.map(this::createPaymentRequest)
				.flatMap(requestData ->
						getWebClient(orgId)
								.createPayment(requestData))
				.flatMap(this::throwExceptionIfNotOk)
				.flatMap(res -> res.bodyToMono(String.class))
				.defaultIfEmpty("-1");
	}



	private Payment createPaymentRequest(PaymentData data) {
		String extOrderId = getExtOrderId(data);
		List<PaymentDetails> paymentDetails = createPaymentDetails(data);

		Payment payment = new Payment();
		payment.setPaymentDetails(paymentDetails);
		payment.setSalesId(extOrderId);
		return payment;
	}



	private String getExtOrderId(PaymentData data) {
		return ofNullable(data)
				.map(PaymentData::getExternalOrderId)
				.orElseThrow(() ->{
					logger.severe(format("Null external order id for payment event[%s]", toString(data)));
					return new ExternalOrderIdNotFound(data);
				});
	}



	private String toString(PaymentData data){
		return ofNullable(data)
				.map(Object::toString)
				.orElse("[]");
	}



	private List<PaymentDetails> createPaymentDetails(PaymentData data) {
		String salesId = data.getExternalOrderId();
		PaymentDetails details = new PaymentDetails();
		details.setAmount(data.getValue());
		details.setSalesId(salesId);
		details.setPaymentMethod(PAYMENT_METHOD);
		return asList(details);
	}



	@Override
	protected PaymentCreateEvent handleError(PaymentCreateEvent event, Throwable t) {
		return event;
	}

}
