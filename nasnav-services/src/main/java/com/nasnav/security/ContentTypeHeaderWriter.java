package com.nasnav.security;

import org.springframework.http.MediaType;
import org.springframework.security.web.header.HeaderWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ContentTypeHeaderWriter implements HeaderWriter {

	@Override
	public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
		if(response.getContentType() == null)
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);	
	}

}
