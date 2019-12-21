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

import com.nasnav.commons.utils.StringUtils;




@Repository
public class OAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

	public static final String AUTH_TOKEN_COOKIE_NAME = "oauth_token";
	public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect";
	public static final String ORG_ID_COOKIE_NAME = "org_id";
	public static Map<String,OAuth2AuthorizationRequest> requests = new HashMap<>();
	
	
	
	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		return CookieUtils.getCookie(request, AUTH_TOKEN_COOKIE_NAME)
			                .map(cookie -> CookieUtils.deserialize(cookie, String.class))
			                .map(requests::get)
			                .orElse(null); 
	}

	
	
	
	
	
	@Override
	public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request,
			HttpServletResponse response) {
		int cookieExpireSeconds = 180;
		String token = UUID.randomUUID().toString();
		CookieUtils.addCookie(response, AUTH_TOKEN_COOKIE_NAME, token, cookieExpireSeconds);
		
		String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (!StringUtils.isBlankOrNull(redirectUriAfterLogin)) {            
			CookieUtils.addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin, cookieExpireSeconds);
        }
        
        String orgId = request.getParameter(ORG_ID_COOKIE_NAME);
        if (!StringUtils.isBlankOrNull(orgId)) {            
			CookieUtils.addCookie(response, ORG_ID_COOKIE_NAME, orgId, cookieExpireSeconds);
        }
        
		requests.put(token,authorizationRequest);
		System.out.println("====> OAUTH2 saved state : " + authorizationRequest.getState() + " for token: " + token);
	}

	
	
	
	
	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request) {
		String token = CookieUtils.getCookie(request, AUTH_TOKEN_COOKIE_NAME)
					                .map(Cookie::getValue)                
					                .orElse("");
		OAuth2AuthorizationRequest oauthReq = requests.get(token);
		requests.remove(token);
		return oauthReq;
	}

}
