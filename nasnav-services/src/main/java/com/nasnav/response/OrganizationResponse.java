package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class OrganizationResponse {

    @JsonProperty(value = "organization_id")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private Long organizationId;

    @JsonProperty(value = "brand_id")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private Long brandId;


    public OrganizationResponse(Long id, int type){
        if (type == 0)
            this.organizationId = id;
        else if (type == 1)
            this.brandId = id;
    }
}
