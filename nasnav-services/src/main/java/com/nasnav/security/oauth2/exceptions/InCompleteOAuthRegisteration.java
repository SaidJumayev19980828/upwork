package com.nasnav.security.oauth2.exceptions;

public class InCompleteOAuthRegisteration extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5487165L;
	
	public InCompleteOAuthRegisteration() {
		super("OAuth registeration was incomplete ,please insert your email!");
	}
}
