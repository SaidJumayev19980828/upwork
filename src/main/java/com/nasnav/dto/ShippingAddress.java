package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

//@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ShippingAddress{
    @JsonProperty("name")
    private String name;
    @JsonProperty("postal_code")
    private Long postalCode;
    @JsonProperty("country")
    private String country;
    @JsonProperty("city")
    private String city;
    @JsonProperty("address")
    private String details;
}
