package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BrandDTO {
    @ApiModelProperty(value = "Brand ID. If empty: new brand", example = "12345")
    @JsonProperty("brand_id")
    public Long id;

    @ApiModelProperty(value = "Operation type .. create or update", example = "create", required = true)
    @JsonProperty("operation")
    public String operation;

    @ApiModelProperty(value = "Brand name, required for creating a brand", example = "Alfa Romeo")
    @JsonProperty("name")
    public String name;

    @ApiModelProperty(value = "Brand p_name, it will be generated automatically if not provided", example = "alfa-romeo")
    @JsonProperty("p_name")
    public String pname;

    @ApiModelProperty(value = "Brand priority, default value is 0 ", example = "2")
    public Integer priority;
}
