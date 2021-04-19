package com.nasnav.service.helpers;

import com.google.common.net.InternetDomainName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class LoginHelper {

	private static Logger logger = LogManager.getLogger();

	private static final String NASNAV_ORG_DOMAIN = "nasnav.org";
	private static final String NASNAV_COM_DOMAIN = "nasnav.com";
	private static final Set<String> ALLOWED_REDIRECT_DOMAINS = setOf(NASNAV_ORG_DOMAIN, NASNAV_COM_DOMAIN, "localhost", "127.0.0.1");
	private static final String DEFAULT_REDIRECT_URL = "/";

	
	
	public static boolean isInvalidRedirectUrl(UriComponents redirectUri, List<String> orgDomains) {
		List<String> orgTopPrivateDomains =
				orgDomains
				.stream()
				.map(LoginHelper::getTopPrivateDomainFromUri)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toList());
		Optional<String> hostTopPrivateDomain =
				ofNullable(redirectUri.getHost())
				.flatMap(LoginHelper::getTopPrivateDomain);
		return !hostTopPrivateDomain.isPresent()
					|| isNotAllowedHost(hostTopPrivateDomain.orElse(""), orgTopPrivateDomains);
	}
	
	
	
	public static boolean isInvalidRedirectUrl(String redirectUriStr, List<String> orgDomains) {
		UriComponents redirectUri = buildUriFromString(redirectUriStr);
		return isInvalidRedirectUrl(redirectUri, orgDomains);
	}




	private static Optional<String> getTopPrivateDomainFromUri(String uriString){
		return ofNullable(uriString)
				.map(LoginHelper::buildUriFromString)
				.map(UriComponents::getHost)
				.flatMap(LoginHelper::getTopPrivateDomain);
	}



	private static Optional<String> getTopPrivateDomain(String host){
		try{
			return ofNullable(host)
					.map(InternetDomainName::from)
					.map(InternetDomainName::topPrivateDomain)
					.map(InternetDomainName::toString);
		}catch(Throwable e){
			logger.error(e,e);
			return empty();
		}
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
	

	
	
	private static boolean isNotAllowedHost(String topPrivateDomain, List<String> orgDomains) {
		List<String> allowedDomains = new ArrayList<>(orgDomains);
		allowedDomains.addAll(ALLOWED_REDIRECT_DOMAINS);
		return allowedDomains
				.stream()
				.noneMatch(domain -> Objects.equals(domain, topPrivateDomain));
	}
	
	
	
	
	private static UriComponents getDefaultRedirectUri() {
		return uriFromString(DEFAULT_REDIRECT_URL);
	}
	
	
	
	
	
	public static UriComponents uriFromString(String uriString){
		String uri = ofNullable(uriString).orElse(DEFAULT_REDIRECT_URL);
		return UriComponentsBuilder.fromUriString(uri).build();
	}
	
	
	
}
