package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.servlet.http.Cookie;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@AllArgsConstructor
@NoArgsConstructor
public class UserApiResponse {

    @JsonProperty(value = "id")
    private Long entityId;
    private List<ResponseStatus> status;
    private String token;
    private List<String> roles;
    private String name;
    private String email;
    @JsonProperty(value = "organization_id")
    private Long organizationId;
    @JsonProperty(value = "store_id")
    private Long storeId;
    @JsonProperty(value = "org_url")
    private String orgUrl;

    @JsonIgnore
    private Cookie cookie;

    public UserApiResponse(Long entityId, String token, List<String> roles, Long organizationId, Long storeId,
                           String name, String email, Cookie cookie) {
        this.entityId = entityId;
        this.name = name;
        this.email = email;
        this.token = token;
        this.roles = roles;
        this.organizationId = organizationId;
        this.storeId = storeId;
        this.cookie = cookie;
    }

    public UserApiResponse(Long entityId, List<ResponseStatus> statuses) {
        this.entityId = entityId;
        this.status = statuses;
    }

    public UserApiResponse(Long entityId, String orgUrl) {
        this.entityId = entityId;
        this.orgUrl = orgUrl;
    }

    public UserApiResponse(List<ResponseStatus> statuses) {
        this.status = statuses;
    }

    public UserApiResponse(Long entityId) {
        this.entityId = entityId;
    }

    public UserApiResponse(Cookie cookie) {
        this.cookie = cookie;
    }
}
