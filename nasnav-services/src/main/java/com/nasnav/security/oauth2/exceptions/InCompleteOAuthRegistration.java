package com.nasnav.security.oauth2.exceptions;

import lombok.Getter;

@Getter
public class InCompleteOAuthRegistration extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5487165L;

	final transient String socialLoginToken;
	
	public InCompleteOAuthRegistration(String socialLoginToken) {
		super("OAuth registration was incomplete ,please insert your email!");
		this.socialLoginToken = socialLoginToken;
	}
}
