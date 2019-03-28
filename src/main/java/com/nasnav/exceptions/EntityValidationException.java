package com.nasnav.exceptions;

import com.nasnav.response.ApiResponse;
import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * Represent all kind of EntityValidationException.
 */
@Data
public class EntityValidationException extends RuntimeException {

    private ApiResponse apiResponse;
    private HttpStatus httpStatus = HttpStatus.BAD_REQUEST; // set default to bad request

    public EntityValidationException(String message) {
        super(message);
    }

    public EntityValidationException(String message, ApiResponse apiResponse) {
        this(message);
        this.apiResponse = apiResponse;
    }

    public EntityValidationException(String message, ApiResponse apiResponse, HttpStatus httpStatus) {
        this(message, apiResponse);
        this.httpStatus = httpStatus;
    }

}
