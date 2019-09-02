package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OrganizationResponse {

    @JsonProperty(value = "success")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private boolean success;

    @JsonProperty(value = "organization_id")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private Long organizationId;

    @JsonProperty(value = "brand_id")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private Long brandId;

    @JsonIgnore
    @Getter
    private HttpStatus httpStatus;


    public OrganizationResponse(Long id, int type){
        this.success = true;
        this.httpStatus = HttpStatus.OK;
        if (type == 0)
            this.organizationId = id;
        else if (type == 1)
            this.brandId = id;
    }


    public OrganizationResponse(){
        this.success = true;
        this.httpStatus = HttpStatus.OK;
    }
}
