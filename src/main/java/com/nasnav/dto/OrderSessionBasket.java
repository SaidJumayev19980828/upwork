package com.nasnav.dto;

import com.nasnav.persistence.BasketsEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderSessionBasket {

    private String name;

    private BigDecimal quantity;

    private BigDecimal price;

}
