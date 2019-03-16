package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;


public class ApiResponse implements Serializable {

    @JsonProperty
    private boolean success;

    // exclude entityId property from
    // json if it is null as per API requirements
    @JsonInclude(JsonInclude.Include.NON_EMPTY)

    // set property name to user_id as per API requirements
    @JsonProperty(value = "user_id")
    private Long entityId;

    // set property name to status as per API requirements
    @JsonProperty(value = "status")
    private List<ResponseStatus> responseStatuses;

    public ApiResponse() {
    }

    /**
     * Constructor representing failed response
     *
     * @param responseStatuses List of ResponseStatus
     */
    public ApiResponse(List<ResponseStatus> responseStatuses) {
        this();
        this.responseStatuses = responseStatuses;
    }

    /**
     * Constructor representing success response
     *
     * @param entityId Entity Object Id
     * @param responseStatuses List of ResponseStatus
     */
    public ApiResponse(Long entityId, List<ResponseStatus> responseStatuses) {
        this(responseStatuses);
        this.success = true;
        this.entityId = entityId;
    }


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public List<ResponseStatus> getResponseStatuses() {
        return responseStatuses;
    }

    public void setResponseStatuses(List<ResponseStatus> responseStatuses) {
        this.responseStatuses = responseStatuses;
    }
}
