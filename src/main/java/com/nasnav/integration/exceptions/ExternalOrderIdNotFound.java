package com.nasnav.integration.exceptions;

import static java.lang.String.format;

import lombok.Getter;


@Getter
public class ExternalOrderIdNotFound extends RuntimeException {

	
	private static final long serialVersionUID = 1845734984L;
	
	
	private Long orderId;
	private Long orgId;
	
	
	
	public ExternalOrderIdNotFound(Long orderId, Long orgId) {		
		super(format("Order with id[%d] has no mapped external id for organization[%d]", orderId, orgId));
		this.orderId = orderId;
	}

}
