package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nasnav.commons.utils.StringUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = false)
public class UserApiResponse  implements Serializable {

	private static final long serialVersionUID = 894752313L;

	// exclude entityId property from
    // json if it is null as per API requirements
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    // set property name to user_id as per API requirements
    @JsonProperty(value = "id")
    private Long entityId;

    // set property name to status as per API requirements
    @JsonProperty(value = "status")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ResponseStatus> responseStatuses;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty(value = "messages")
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

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty(value = "name")
    private String name;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty(value = "email")
    private String email;

    public UserApiResponse(boolean success, Long entityId, List<ResponseStatus> responseStatuses,
                           String token, List<String> roles, Long organizationId, Long storeId,
                           String name, String email) {
        this.entityId = entityId;
        this.name = name;
        this.email = email;
        this.responseStatuses = responseStatuses;
        this.token = token;
        this.roles = roles;
        this.organizationId = organizationId;
        this.storeId = storeId;
    }

    public UserApiResponse(){

    }

    /**
     * Constructor representing failed response
     *
     * @param responseStatuses List of ResponseStatus
     */
    private UserApiResponse(List<ResponseStatus> responseStatuses) {
        this();
        this.responseStatuses = responseStatuses;
    }

    /**
     * Constructor representing success response
     *
     * @param entityId Entity Object Id
     * @param responseStatuses List of ResponseStatus
     */
    private UserApiResponse(Long entityId, List<ResponseStatus> responseStatuses) {
        this(responseStatuses);
        this.entityId = entityId;
    }

    /**
     * constructor that return message property in response
     * @param success
     * @param responseStatuses
     */
    private UserApiResponse(boolean success, List<ResponseStatus> responseStatuses) {
        if(StringUtils.isNotBlankOrNull(responseStatuses)){
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
     * @return UserApiResponse object holding message property
     */
    public static UserApiResponse createMessagesApiResponse(boolean success, List<ResponseStatus> responseStatuses){
        return new UserApiResponse(success, responseStatuses);
    }


    /**
     * @param entityId entityId
     * @param responseStatuses success statuses
     * @return UserApiResponse object holding status property
     */
    public static UserApiResponse createStatusApiResponse(Long entityId, List<ResponseStatus> responseStatuses){
        return new UserApiResponse(entityId, responseStatuses);
    }


    /**
     *
     * @param responseStatuses failed statuses
     * @return UserApiResponse object holding status property
     */
    public static UserApiResponse createStatusApiResponse(List<ResponseStatus> responseStatuses){
        return new UserApiResponse( responseStatuses);
    }

}
