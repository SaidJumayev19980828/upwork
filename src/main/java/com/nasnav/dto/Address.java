package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Address{

    private Object area;
    private String country;
    private Object city;
    private String street;
    private String floor;
    private String lat;
    private String lng;
}
