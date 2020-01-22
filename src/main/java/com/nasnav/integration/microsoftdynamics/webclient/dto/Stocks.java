package com.nasnav.integration.microsoftdynamics.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Stocks {

    @JsonProperty("STORE_CODE")
    private String storeCode;
    @JsonProperty("Value")
    private BigDecimal value;
}
