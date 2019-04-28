package com.nasnav.dto;

import com.nasnav.persistence.BasketsEntity;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderSessionResponse {

    private boolean success;

    private String session_id;

    private String merchant_id;

    private String order_ref;

    private List<OrderSessionBasket> basket;

    private BigDecimal order_value;

    private Currency order_currency;

}
