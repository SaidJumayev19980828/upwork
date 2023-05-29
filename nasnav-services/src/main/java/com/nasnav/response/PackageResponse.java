package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PackageResponse {

    @JsonProperty(value = "name")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private String name;

    @JsonProperty(value = "description")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private String description;

    @JsonProperty(value = "price")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private BigDecimal price;

}
