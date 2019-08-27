package com.nasnav.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.response.BaseResponse;

public class NasnavHttpStatusEntryPoint implements AuthenticationEntryPoint {
	
	private HttpStatus status;
	
	
	public NasnavHttpStatusEntryPoint(HttpStatus status) {
		this.status = status;
	}
	
	
	

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		String jsonInString = createResponseBody();

		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);		
		response.getWriter().write(jsonInString);
	}
	
	
	

	private String createResponseBody() throws JsonProcessingException {
		BaseResponse body = new BaseResponse(false);
		ObjectMapper mapper = new ObjectMapper();		
		String jsonInString = mapper.writeValueAsString(body);
		return jsonInString;
	}

}
