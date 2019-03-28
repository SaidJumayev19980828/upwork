package com.nasnav.response.exception;

import com.nasnav.enumerations.OrderFailedStatus;

/**
 * Represent all kind of EntityValidationException
 */
public class OrderValidationException extends RuntimeException {

    private OrderFailedStatus status;

    public OrderValidationException(String message) {
        super(message);
    }

    public OrderValidationException(String message, OrderFailedStatus status) {
        this(message);
        this.status = status;
    }

    
    public OrderFailedStatus getStatus() {
        return status;
    }

}
