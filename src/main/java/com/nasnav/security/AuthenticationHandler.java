package com.nasnav.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.response.BaseResponse;



public class AuthenticationHandler implements AuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
			throws IOException, ServletException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		
		String jsonInString = createResponseBody();

		response.getWriter().write(jsonInString);

	}
	
	
	private String createResponseBody() throws JsonProcessingException {
		BaseResponse body = new BaseResponse(false);
		ObjectMapper mapper = new ObjectMapper();		
		String jsonInString = mapper.writeValueAsString(body);
		return jsonInString;
	}


}
