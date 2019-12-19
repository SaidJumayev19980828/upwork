package com.nasnav.security.oauth2;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Repository;




@Repository
public class OAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

	private static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "nasnav_cookie";
	public static Map<String,OAuth2AuthorizationRequest> requests = new HashMap<>();
	
	
	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		return CookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
			                .map(cookie -> CookieUtils.deserialize(cookie, String.class))
			                .map(requests::get)
			                .orElse(null);
	}

	
	
	
	
	
	@Override
	public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request,
			HttpServletResponse response) {
		String token = UUID.randomUUID().toString();
		CookieUtils.addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, token, 180);
		requests.put(token,authorizationRequest);
		System.out.println("====> OAUTH2 saved state : " + authorizationRequest.getState() + " for token: " + token);
	}

	
	
	
	
	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request) {
		String token = CookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
					                .map(cookie -> CookieUtils.deserialize(cookie, String.class))                
					                .orElse("");
		OAuth2AuthorizationRequest oauthReq = requests.get(token);
		requests.remove(token);
		return oauthReq;
	}

}
