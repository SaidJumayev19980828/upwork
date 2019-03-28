package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nasnav.persistence.EntityUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
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
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ResponseStatus> responseStatuses;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty(value = "message")
    private List<String> messages;


    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty(value = "token")
    private String token;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty(value = "roles")
    private List<String> roles;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty(value = "organization_id")
    private Long organizationId;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty(value = "store_id")
    private Long storeId;

    public ApiResponse(boolean success, Long entityId, List<ResponseStatus> responseStatuses,
                         String token, List<String> roles, Long organizationId, Long storeId) {
        this.success = success;
        this.entityId = entityId;
        this.responseStatuses = responseStatuses;
        this.token = token;
        this.roles = roles;
        this.organizationId = organizationId;
        this.storeId = storeId;
    }

    public ApiResponse(){

    }

    /**
     * Constructor representing failed response
     *
     * @param responseStatuses List of ResponseStatus
     */
    private ApiResponse(List<ResponseStatus> responseStatuses) {
        this();
        this.responseStatuses = responseStatuses;
    }

    /**
     * Constructor representing success response
     *
     * @param entityId Entity Object Id
     * @param responseStatuses List of ResponseStatus
     */
    private ApiResponse(Long entityId, List<ResponseStatus> responseStatuses) {
        this(responseStatuses);
        this.success = true;
        this.entityId = entityId;
    }

    /**
     * constructor that return message property in response
     * @param success
     * @param responseStatuses
     */
    private ApiResponse(boolean success, List<ResponseStatus> responseStatuses) {
        this.success = success;
        if(EntityUtils.isNotBlankOrNull(responseStatuses)){
            List<String> messagesList= new ArrayList<>();
            responseStatuses.forEach(responseStatus->{
                messagesList.add(responseStatus.name());
            });
            this.messages = messagesList;
        }
    }

    /**
     *
     * @param success
     * @param responseStatuses
     * @return ApiResponse object holding message property
     */
    public static ApiResponse  createMessagesApiResponse(boolean success, List<ResponseStatus> responseStatuses){
        return new ApiResponse(success, responseStatuses);
    }


    /**
     * @param entityId entityId
     * @param responseStatuses success statuses
     * @return ApiResponse object holding status property
     */
    public static ApiResponse  createStatusApiResponse(Long entityId, List<ResponseStatus> responseStatuses){
        return new ApiResponse(entityId, responseStatuses);
    }


    /**
     *
     * @param responseStatuses failed statuses
     * @return ApiResponse object holding status property
     */
    public static ApiResponse  createStatusApiResponse( List<ResponseStatus> responseStatuses){
        return new ApiResponse( responseStatuses);
    }

}
