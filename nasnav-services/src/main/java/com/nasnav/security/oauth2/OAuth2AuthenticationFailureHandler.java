package com.nasnav.security.oauth2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.nasnav.security.oauth2.OAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    AuthorizationRequestRepository<OAuth2AuthorizationRequest> requestRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {

        logger.error(exception, exception);

    	String targetUrl = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
						                .map(Cookie::getValue)
						                .orElse(("/"));

        targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
						                .queryParam("error", exception.getLocalizedMessage())
						                .build()
						                .toUriString();

        requestRepository.removeAuthorizationRequest(request, response);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}