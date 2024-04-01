package com.nasnav.dto;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RuleTier(
        @NotNull(message = "condition is required cannot be null")
        long condition,
        @NotNull(message = "reward is required cannot be null")
        BigDecimal reward,
        boolean isActive
) {
}
