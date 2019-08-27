package com.nasnav.response;

import java.io.Serializable;
import java.util.List;


/**
 * Implementation of Builder Design pattern to create
 * Api response object to be sent at response
 */
public class ApiResponseBuilder implements Serializable {

    private boolean success;
    private Long entityId;
    private List<ResponseStatus> responseStatuses;
    private String token;
    private List<String> roles;
    private Long organizationId;
    private Long storeId;
    private String name;
    private String email;

    public ApiResponseBuilder() {
    }

    public UserApiResponse build() {
        return new UserApiResponse(success, entityId, responseStatuses,
                token, roles, organizationId, storeId, name, email);
    }

    public ApiResponseBuilder setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public ApiResponseBuilder setEntityId(Long entityId) {
        this.entityId = entityId;
        return this;
    }

    public ApiResponseBuilder setResponseStatuses(List<ResponseStatus> responseStatuses) {
        this.responseStatuses = responseStatuses;
        return this;
    }

    public ApiResponseBuilder setToken(String token) {
        this.token = token;
        return this;
    }

    public ApiResponseBuilder setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    public ApiResponseBuilder setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    public ApiResponseBuilder setStoreId(Long storeId) {
        this.storeId = storeId;
        return this;
    }

    public ApiResponseBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ApiResponseBuilder setEmail(String email) {
        this.email = email;
        return this;
    }
}
