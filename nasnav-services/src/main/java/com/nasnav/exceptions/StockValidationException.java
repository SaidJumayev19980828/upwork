package com.nasnav.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@Data
@EqualsAndHashCode(callSuper = true)
public class StockValidationException extends RuntimeBusinessException {

	private static final long serialVersionUID = 548745166L;
	private Integer index;
	
	public StockValidationException(String errorMessage, String errorCode, HttpStatus httpStatus, Integer index) {
    	super(errorMessage, errorCode, httpStatus);
    	this.index = index;
    }
	
	
	
	public StockValidationException(String errorMessage, String errorCode, HttpStatus httpStatus) {
    	super(errorMessage, errorCode, httpStatus);
    	this.index = 0;
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
