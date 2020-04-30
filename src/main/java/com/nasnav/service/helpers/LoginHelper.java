package com.nasnav.service.helpers;

import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static java.util.Optional.ofNullable;

import java.util.Set;

import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class LoginHelper {

	private static final String NASNAV_DOMAIN = "nasnav.org";
	private static final Set<String> ALLOWED_REDIRECT_DOMAINS = setOf(NASNAV_DOMAIN, "localhost", "127.0.0.1");
	private static final String DEFAULT_REDIRECT_URL = "/";

	
	
	public static boolean isInvalidRedirectUrl(UriComponents redirectUri) {
		String host = redirectUri.getHost();
		return host != null && isNotAllowedHost(host);
	}
	
	
	
	public static boolean isInvalidRedirectUrl(String redirectUriStr) {
		UriComponents redirectUri = buildUriFromString(redirectUriStr);
		String host = redirectUri.getHost();
		return host != null && isNotAllowedHost(host);
	}


	
	
	public static UriComponents buildUriFromString(String uriString) {
		return ofNullable(uriString)
				.map(UriComponentsBuilder::fromUriString)
				.map(UriComponentsBuilder::build)
				.orElseGet(LoginHelper::getDefaultRedirectUri);
	}
	
	
	
	public static UriComponents buildUriFromString(String uriString, String defaultUri) {
		return ofNullable(uriString)
				.map(UriComponentsBuilder::fromUriString)
				.map(UriComponentsBuilder::build)
				.orElseGet(() -> uriFromString(defaultUri));
	}
	

	
	
	private static boolean isNotAllowedHost(String host) {
		return ALLOWED_REDIRECT_DOMAINS
				.stream()
				.noneMatch(domain -> host.endsWith(domain));
	}
	
	
	
	
	private static UriComponents getDefaultRedirectUri() {
		return uriFromString(DEFAULT_REDIRECT_URL);
	}
	
	
	
	
	
	public static UriComponents uriFromString(String uriString){
		String uri = ofNullable(uriString).orElse(DEFAULT_REDIRECT_URL);
		return UriComponentsBuilder.fromUriString(uri).build();
	}
	
	
	
}
