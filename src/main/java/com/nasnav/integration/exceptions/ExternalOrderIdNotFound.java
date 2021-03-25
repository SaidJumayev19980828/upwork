package com.nasnav.integration.exceptions;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import com.nasnav.integration.events.data.PaymentData;
import lombok.Getter;


@Getter
public class ExternalOrderIdNotFound extends RuntimeException {

	private static final long serialVersionUID = 1845734984L;
	
	private final Long orderId;


	public ExternalOrderIdNotFound(PaymentData payment ) {
		super(format("Order with id[%d] has no mapped external id for organization[%d]", getOrderId(payment), getOrgId(payment)));
		this.orderId = getOrderId(payment);
	}



	private static Long getOrgId(PaymentData payment) {
		return ofNullable(payment).map(PaymentData::getOrganizationId).orElse(null);
	}



	private static Long getOrderId(PaymentData payment) {
		return ofNullable(payment).map(PaymentData::getOrderId).orElse(null);
	}

}
