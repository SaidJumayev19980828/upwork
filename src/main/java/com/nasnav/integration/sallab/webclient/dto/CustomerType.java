package com.nasnav.integration.sallab.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CustomerType {

    @JsonProperty("Cust_Type__c")
    private Integer customerType;
}
