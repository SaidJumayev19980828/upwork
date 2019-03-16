package com.nasnav.response.exception;

import com.nasnav.response.ApiResponse;
import com.nasnav.response.ResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Collections;

/**
 * Custom exception handling class to handle all exception resulted from
 * any request.
 */

@ControllerAdvice
@RestController
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private final Logger exceptionLogger = LoggerFactory.getLogger(RestExceptionHandler.class.getName());

    /**
     * Handle EntityValidationException exception
     *
     * @param ex      EntityValidationException to be handled
     * @param request WebRequest that result in that EntityValidationException
     * @return ApiResponse object to the requester
     */
    @ExceptionHandler(EntityValidationException.class)
    public final ApiResponse handleValidationException(EntityValidationException ex, WebRequest request) {
        this.logException(request, ex);
        return new ApiResponse(ex.getResponseStatusList());
    }

    /**
     * Handle all non explicitly handled exceptions
     *
     * @param ex      Exception to be handled
     * @param request WebRequest that result in that Exception
     * @return ApiResponse object to the requester
     */
    @ExceptionHandler(Exception.class)
    public final ApiResponse handleAllExceptions(Exception ex, WebRequest request) {
        this.logException(request, ex);
        return new ApiResponse(Collections.singletonList(ResponseStatus.SYS_ERROR));
    }

    /**
     * Log failed request with exception details
     *
     * @param request WebRequest that result in that Exception
     * @param ex      Exception to be handled
     */
    private void logException(WebRequest request, Exception ex) {
        exceptionLogger.error(" Exception: Unable to process this request :  " + request.getDescription(false), ex);
    }

}
