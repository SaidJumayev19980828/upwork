package com.nasnav.integration.sallab.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CustomerDTO {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Birth_Day__c")
    private String birthDay;

    @JsonProperty("Birth_Month__c")
    private String birthMonth;

    @JsonProperty("Cus_Email__c")
    private String customerEmail;

    @JsonProperty("Cus_Mobile__c")
    private String customerMobile;

    @JsonProperty("Cus_Address__c")
    private String customerAddress;

    @JsonProperty("Sal_Area__r")
    private SalArea salArea;

    @JsonProperty("Cust_Type__r")
    private CustomerType customerType;
}
