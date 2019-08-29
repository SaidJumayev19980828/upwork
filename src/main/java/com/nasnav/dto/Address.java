package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Address{

    @JsonProperty("area")
    private Object area;
    @JsonProperty("p_area")
    private String parea;
    @JsonProperty("country")
    private String country;
    @JsonProperty("city")
    private Object city;
    @JsonProperty("street")
    private String street;
    @JsonProperty("floor")
    private String floor;
    @JsonProperty("lat")
    private String lat;
    @JsonProperty("lng")
    private String lng;
}
