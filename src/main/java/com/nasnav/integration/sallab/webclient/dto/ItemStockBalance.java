package com.nasnav.integration.sallab.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemStockBalance {

    @JsonProperty("QTY")
    private BigDecimal quantity;

    @JsonProperty("FREE_BAL")
    private BigDecimal freeBalance;

    @JsonProperty("RESV_BAL")
    private BigDecimal reserveBalance;

    @JsonProperty("STK_NO")
    private Integer stockId;

    @JsonProperty("STK_NAME")
    private String stockName;

    @JsonProperty("ESTK_NAME")
    private String englishStockName;

    @JsonProperty("STK_ADDRESS")
    private String stockAddress;

    @JsonProperty("STK_STR_ID")
    private String stockStringId;
}
