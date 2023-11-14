package com.nasnav.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import static java.lang.String.format;

@Getter
@Setter
public class CustomException extends RuntimeException {
    private static final long serialVersionUID = 8246015770249962894L;
    
    private final String message;
    private final HttpStatus httpStatus;

    public CustomException(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
    public CustomException(ErrorCodes message, Object args, HttpStatus httpStatus) {
        this.message = format(message.getValue(), args);
        this.httpStatus = httpStatus;
    }

    public CustomException(ErrorCodes message, HttpStatus httpStatus) {
        this.message = message.getValue();
        this.httpStatus = httpStatus;
    }
    
    public CustomException(String message, HttpStatus httpStatus, Throwable cause) {
        super(cause);
        this.message = message;
        this.httpStatus = httpStatus;
    }

}
