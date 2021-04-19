package com.nasnav.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSessionBasket {

    private String name;

    private BigDecimal quantity;

    private BigDecimal price;

}
