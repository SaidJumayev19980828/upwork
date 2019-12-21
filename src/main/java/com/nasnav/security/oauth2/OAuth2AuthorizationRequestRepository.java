package com.nasnav.security.oauth2;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Repository;

import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;




@Repository
public class OAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

	public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
	public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect";
	public static final String ORG_ID_COOKIE_NAME = "org_id";
	public static Map<String,OAuth2AuthorizationRequest> requests = new HashMap<>();
	
	
	
	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		return CookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
		                .map(cookie -> CookieUtils.deserialize(cookie, OAuth2AuthorizationRequest.class))
		                .orElse(null);
	}

	
	
	
	
	
	@Override
	public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request,
			HttpServletResponse response) {
		int cookieExpireSeconds = 180;
		
		 if (authorizationRequest == null) {
	            CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
	            CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
	            CookieUtils.deleteCookie(request, response, ORG_ID_COOKIE_NAME);
	            return;
	     }
		 
		
		String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
		String orgId = request.getParameter(ORG_ID_COOKIE_NAME);
		
		if(isBlankOrNull(redirectUriAfterLogin) || isBlankOrNull(orgId)) {
			throw new IllegalStateException("Missing OAuth2 request parameters! redirect url and organization id must be provided!");
		}
		
		CookieUtils.addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, CookieUtils.serialize(authorizationRequest), cookieExpireSeconds);
		CookieUtils.addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin, cookieExpireSeconds);
		CookieUtils.addCookie(response, ORG_ID_COOKIE_NAME, orgId, cookieExpireSeconds);		
	}

	
	
	
	
	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request) {
		return this.loadAuthorizationRequest(request);		
	}

}
