package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OrganizationResponse {

    @JsonProperty(value = "success")
    private boolean success;

    @JsonProperty(value = "organization_id")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private Long organizationId;

    @JsonProperty(value = "status")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private String status;

    @JsonProperty(value = "description")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private String description;

    @JsonIgnore
    @Getter
    private HttpStatus httpStatus;


    public OrganizationResponse(Long organizationId){
        this.success = true;
        this.organizationId = organizationId;
        this.httpStatus = HttpStatus.OK;
    }

    public OrganizationResponse(String status, String description){
        this.success = false;
        this.status = status;
        this.description = description;
        this.httpStatus = HttpStatus.NOT_ACCEPTABLE;
    }
    public OrganizationResponse(){
        this.success = true;
        this.httpStatus = HttpStatus.OK;
    }
}
