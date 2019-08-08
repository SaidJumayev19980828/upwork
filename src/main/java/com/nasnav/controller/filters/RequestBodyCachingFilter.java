package com.nasnav.controller.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;


/**
 * A filter for wrapping the servlet request as ContentCachingRequestWrapper, which caches the request
 * body and allow us to retrieve it multiple time on other filters.
 * 
 * We need this filter because HTTPServletRequest.getInputStream() returns the request payload, and the
 * stream is closed after being called once.
 * But ContentCachingRequestWrapper is an implementation that caches the payload into it, and it can be called using
 * ContentCachingRequestWrapper.getContentAsByteArray(), which can be called multiple times.
 * @2019-07-29 We used this mainly to pass the request body to ExceptionHandler, so we can log the request body along
 * with the request parameters and the exception that was thrown		
 * */

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class RequestBodyCachingFilter extends OncePerRequestFilter{

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {		
		
		//caches the request body , so we can access it in the Exception Handler
		ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);

        // pass through filter chain to do the actual request handling
        filterChain.doFilter(wrappedRequest, response);
		
	}

}
