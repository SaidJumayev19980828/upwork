package com.nasnav.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Schema
public record CompensationRule(
        @NotNull(message = "Compensation action is mandatory cannot be Null ")
        long action,
        @NotNull(message = "Rule name is mandatory cannot be Null ")
        String name,

        String description,

        @NotNull(message = "Rule is active cannot be Null ")
        boolean isActive,

        @NotNull(message = "Rule tiers are mandatory cannot be Null ")
        Set<RuleTier> tiers

) {
}
