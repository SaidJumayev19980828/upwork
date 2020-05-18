package com.nasnav.security;


import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class AuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    
    
    
    

    AuthenticationFilter(final RequestMatcher requiresAuth) {
        super(requiresAuth);
    }
    
    
    

    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException, IOException, ServletException {

        String token = "";
        Cookie[] cookies = httpServletRequest.getCookies();

        if ( cookies != null && cookies.length != 0) {
            for(Cookie c : cookies) {
                if (Objects.equals(c.getName(), TOKEN_HEADER)) {
                    token = c.getValue();
                    break;
                }
            }
        } else {
            token = httpServletRequest.getHeader(TOKEN_HEADER);
        }
        UsernamePasswordAuthenticationToken requestAuthentication = new UsernamePasswordAuthenticationToken(token, token);
        return getAuthenticationManager().authenticate(requestAuthentication);
    }

    
    
    
    
    @Override
    protected void successfulAuthentication(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain, final Authentication authResult) throws IOException, ServletException {
        SecurityContextHolder.getContext().setAuthentication(authResult);
        chain.doFilter(request, response);
    }
}