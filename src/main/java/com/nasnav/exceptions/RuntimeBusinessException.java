package com.nasnav.exceptions;

import org.springframework.http.HttpStatus;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RuntimeBusinessException extends RuntimeException implements BusinessExceptionInterface{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8526630656781428983L;

	private String errorMessage;

    private String errorCode;

    private HttpStatus httpStatus;

    public RuntimeBusinessException(String errorMessage, String errorCode, HttpStatus httpStatus) {
    	super(errorMessage);
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    

    public RuntimeBusinessException(String errorMessage, String errorCode, HttpStatus httpStatus, Throwable cause) {
        initCause(cause);
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    
    
    public RuntimeBusinessException(BusinessException e) {
    	super(e.getErrorMessage());
        this.errorMessage = e.getErrorMessage();
        this.errorCode = e.getErrorCode();
        this.httpStatus = e.getHttpStatus();
    }
}
