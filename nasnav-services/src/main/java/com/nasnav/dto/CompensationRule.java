package com.nasnav.dto;

import javax.validation.constraints.NotNull;
import java.util.Set;

public record CompensationRule(
        @NotNull(message = "Compensation action is mandatory cannot be Null ")
        long action,
        @NotNull(message = "Rule name is mandatory cannot be Null ")
        String name,
        boolean isActive,

        @NotNull(message = "Rule tiers are mandatory cannot be Null ")
        Set<RuleTier> tiers

) {
}
