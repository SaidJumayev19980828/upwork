package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
public class OrganizationDTO {

    @ApiModelProperty(value = "Organization's name", example = "Super Pharm", required = true)
    @JsonProperty("name")
    public String name;

    @ApiModelProperty(value = "Url-compatible name, used as part of the URL path for organization shop", example = "super-pharm", required = true)
    @JsonProperty("p_name")
    public String pname;
}
