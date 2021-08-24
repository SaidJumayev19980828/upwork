package com.nasnav.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class NasnavAccessDeniedHandler implements AccessDeniedHandler {

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
		return mapper.writeValueAsString(body);
	}

}
