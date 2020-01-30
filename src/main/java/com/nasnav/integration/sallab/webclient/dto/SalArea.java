package com.nasnav.integration.sallab.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SalArea {

    @JsonProperty("Area_Code__c")
    private Integer areaCode;
}
