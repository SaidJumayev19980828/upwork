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

    @JsonProperty("customer")
    private Customer customer;

    @JsonProperty("seller")
    private Seller seller;

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
    private TransactionCurrency order_currency;

    @Data
    public static class ResponseSession{
        @JsonProperty("id")
        private String id;
    }

    @Data
    public static class Customer{
        @JsonProperty("name")
        private String name;

        @JsonProperty("email")
        private String email;
    }

    @Data
    public static class Seller{
        @JsonProperty("organizationName")
        private String name;

        @JsonProperty("address")
        private String address;

        @JsonProperty("organizationUrl")
        private String url;
    }


}
