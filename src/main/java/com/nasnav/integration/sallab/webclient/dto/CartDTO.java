package com.nasnav.integration.sallab.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class CartDTO {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("StageName")
    private String stageName;

    @JsonProperty("AccountId")
    private String accountId;

    @JsonProperty("CloseDate")
    private String closeDate; //yyyy-MM-dd

    @JsonProperty("Sales_Rep__c")
    private String salesRepresentativeCode;

}
