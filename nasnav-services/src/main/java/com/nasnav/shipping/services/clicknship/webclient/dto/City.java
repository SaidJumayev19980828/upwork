package com.nasnav.shipping.services.clicknship.webclient.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@AllArgsConstructor
public class City {
    private String cityCode;
    private String cityName;
}
