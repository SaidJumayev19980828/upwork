package com.nasnav.security.oauth2.exceptions;

public class InCompleteOAuthRegistration extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5487165L;
	
	public InCompleteOAuthRegistration() {
		super("OAuth registration was incomplete ,please insert your email!");
	}
}
