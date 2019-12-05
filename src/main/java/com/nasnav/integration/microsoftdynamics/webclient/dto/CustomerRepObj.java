package com.nasnav.integration.microsoftdynamics.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CustomerRepObj {

    @JsonProperty("ACCOUNTNUM")
    private String accountNumber;
    @JsonProperty("Customer Name")
    private String name;
    @JsonProperty("Phone Number")
    private String phoneNumber;
}
