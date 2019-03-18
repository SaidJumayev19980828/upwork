package com.nasnav.response.exception;

import com.nasnav.response.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent all kind of EntityValidationException
 */
public class EntityValidationException extends RuntimeException {

    private List<ResponseStatus> responseStatusList;

    public EntityValidationException(String message) {
        super(message);
    }

    public EntityValidationException(String message, List<ResponseStatus> responseStatusList) {
        this(message);
        this.responseStatusList = responseStatusList;
    }

    public void addResponseStatus(ResponseStatus responseStatus) {
        if (this.responseStatusList == null) {
            this.responseStatusList = new ArrayList<>();
        }
        this.responseStatusList.add(responseStatus);
    }

    public List<ResponseStatus> getResponseStatusList() {
        return responseStatusList;
    }

}
