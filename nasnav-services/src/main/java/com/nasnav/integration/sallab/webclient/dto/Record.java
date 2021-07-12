package com.nasnav.integration.sallab.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Record {
    public Attributes attributes;
    @JsonProperty("Id")
    public String id;
    @JsonProperty("UnitPrice")
    public BigDecimal unitPrice;
    @JsonProperty("Product2")
    public Product product;
}
