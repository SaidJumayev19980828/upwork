package com.nasnav.security.oauth2.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RegisterationEmailExistsException extends AuthenticationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5894536L;
	

	public RegisterationEmailExistsException(String format) {
		super(format);
	}

}
