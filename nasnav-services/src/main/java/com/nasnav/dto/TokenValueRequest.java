package com.nasnav.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TokenValueRequest {
    private String currency;
    private BigDecimal amount;
}
