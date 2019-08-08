package com.nasnav.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.response.BaseResponse;

public class NasnavAccessDeniedEntryPoint implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		
		response.setStatus(HttpStatus.FORBIDDEN.value());
		
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
