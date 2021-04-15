package com.nasnav.integration.exceptions;

import com.nasnav.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidIntegrationEventException extends BusinessException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5854845184L;

	public InvalidIntegrationEventException(String errorMessage, String errorCode, HttpStatus httpStatus) {
		super(errorMessage, errorCode, httpStatus);
	}

}
