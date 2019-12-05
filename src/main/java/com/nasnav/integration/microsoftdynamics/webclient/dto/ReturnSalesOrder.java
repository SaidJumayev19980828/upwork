package com.nasnav.integration.microsoftdynamics.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ReturnSalesOrder {
    @JsonProperty("SalesId")
    private String salesId;
    @JsonProperty("Items")
    private List<ReturnSalesOrderItem> items;
}
