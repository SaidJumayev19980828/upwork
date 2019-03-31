package com.nasnav.exceptions;

import com.nasnav.response.UserApiResponse;
import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * Represent all kind of EntityValidationException.
 */
@Data
public class EntityValidationException extends RuntimeException {

    private UserApiResponse userApiResponse;
    private HttpStatus httpStatus = HttpStatus.BAD_REQUEST; // set default to bad request

    public EntityValidationException(String message) {
        super(message);
    }

    public EntityValidationException(String message, UserApiResponse userApiResponse) {
        this(message);
        this.userApiResponse = userApiResponse;
    }

    public EntityValidationException(String message, UserApiResponse userApiResponse, HttpStatus httpStatus) {
        this(message, userApiResponse);
        this.httpStatus = httpStatus;
    }

}
