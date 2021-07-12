package com.nasnav.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@Data
@EqualsAndHashCode(callSuper = false)
public class BusinessException extends Exception implements BusinessExceptionInterface{


    /**
	 * 
	 */
	private static final long serialVersionUID = -8526630656781493983L;

	private String errorMessage;

    private String errorCode;

    private HttpStatus httpStatus;
    
    
    public BusinessException(String errorMessage, ErrorCodes errorCode, HttpStatus httpStatus) {
    	super(errorMessage);
        this.errorMessage = errorMessage;
        this.errorCode = errorCode.toString();
        this.httpStatus = httpStatus;
    }

    public BusinessException(String errorMessage, String errorCode, HttpStatus httpStatus) {
    	super(errorMessage);
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public BusinessException(String errorMessage, String errorCode, HttpStatus httpStatus, Throwable cause) {
        initCause(cause);
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
