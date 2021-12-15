package com.nasnav.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class LoyaltyPointsCartResponseDto {
    private Long orgId;
    private Integer points;
    private BigDecimal amount;
}
