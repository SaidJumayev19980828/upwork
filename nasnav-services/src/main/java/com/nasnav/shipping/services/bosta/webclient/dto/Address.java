package com.nasnav.shipping.services.bosta.webclient.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Address {
    private String firstLine;
    private String secondLine;
    private String floor;
    private String buildingNumber;
    private Long apartment;
    private String zone;
    private String district;
    private String city;
}
