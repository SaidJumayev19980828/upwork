package com.nasnav.shipping.services.bosta.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BostaCallbackDTO {
    @JsonProperty("_id")
    private String id;
    private Integer state;
    private String starName;
    private BigDecimal cod;
    private String exceptionReason;
    private BigDecimal price;
    private BigDecimal weight;
}
