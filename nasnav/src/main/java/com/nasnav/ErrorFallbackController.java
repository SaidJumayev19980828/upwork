package com.nasnav;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.Optional.ofNullable;
import static javax.servlet.RequestDispatcher.ERROR_STATUS_CODE;

@Controller
public class ErrorFallbackController implements ErrorController{
	
	@RequestMapping("/error")
    public void transparentErrorHandler(HttpServletRequest request, HttpServletResponse response) {
		Integer statusCode =
				ofNullable(request.getAttribute(ERROR_STATUS_CODE))
				.map(Object::toString)
				.map(Integer::valueOf)
				.orElse(500);
		
	    response.setStatus(statusCode);
    }
	
	
	

	@Override
	public String getErrorPath() {
		return "/error";
	}

}
