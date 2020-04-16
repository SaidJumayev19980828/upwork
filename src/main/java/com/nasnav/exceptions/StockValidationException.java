package com.nasnav.exceptions;

import org.springframework.http.HttpStatus;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StockValidationException extends RuntimeBusinessException {

	private static final long serialVersionUID = 548745166L;
	private Integer index;
	
	public StockValidationException(String errorMessage, String errorCode, HttpStatus httpStatus, Integer index) {
    	super(errorMessage, errorCode, httpStatus);
    	this.index = index;
    }
    
    

    public StockValidationException(String errorMessage, String errorCode, HttpStatus httpStatus, Throwable cause, Integer index) {
        super(errorMessage, errorCode, httpStatus, cause);
        this.index = index;
    }
    
    
    
    public StockValidationException(BusinessException e, Integer index) {
    	super(e);
    	this.index = index;
    }
}
