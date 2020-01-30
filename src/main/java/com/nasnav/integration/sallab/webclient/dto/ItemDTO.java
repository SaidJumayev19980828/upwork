package com.nasnav.integration.sallab.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class ItemDTO {

    @JsonProperty("PricebookEntryId")
    public String pricebookEntryId;
    @JsonProperty("UnitPrice")
    public BigDecimal unitPrice;
    @JsonProperty("Quantity")
    public BigDecimal quantity;
    @JsonProperty("Stk_Str__c")
    public String stockStringC;
    @JsonProperty("OpportunityId")
    public String opportunityId;
    @JsonProperty("Pack_Closing__c")
    public Double packClosingC;

}
