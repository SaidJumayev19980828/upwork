package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.persistence.BasketsEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel(value = "Returned Session Order")
public class OrderSessionResponse {

    @JsonProperty("success")
    private boolean success;

//    @JsonProperty("successIndicator")
//    private String successIndicator;

//    @JsonProperty("api_version")
//    private String apiVersion;

    @JsonProperty("merchant")
    private String merchantId;

    @JsonProperty("order_id")
    private String orderRef;

    @JsonProperty("order_amount")
    private BigDecimal orderAmount;

    @JsonProperty("order_currency")
    private TransactionCurrency orderCurrency;

    @JsonProperty("execute_url")
    private String executeUrl;

    @JsonProperty("success_url")
    private String successUrl;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("api_url")
    private String apiUrl;

    @JsonProperty("script_url")
    private String scriptUrl;

}
