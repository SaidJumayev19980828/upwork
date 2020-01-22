package com.nasnav.integration.microsoftdynamics.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReturnSalesOrderItem {
    @JsonProperty("SalesId")
    private String salesId;
    @JsonProperty("Item")
    private String item;
    @JsonProperty("Qty")
    private BigDecimal quantity;
}
