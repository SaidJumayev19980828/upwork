package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nasnav.persistence.BasketsEntity;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderSessionResponse implements Serializable {

    private boolean success;

    @JsonProperty("session")
    private ResponseSession session;

    @JsonProperty("merchant")
    private String merchant_id;

    @JsonProperty("id")
    private String order_ref;

    @JsonProperty("basket")
    private List<OrderSessionBasket> basket;

    @JsonProperty("order_value")
    private BigDecimal order_value;

    @JsonProperty("currency")
    private Currency order_currency;

    @Data
    public static class ResponseSession implements Serializable{
        @JsonProperty("id")
        private String id;
    }

}
