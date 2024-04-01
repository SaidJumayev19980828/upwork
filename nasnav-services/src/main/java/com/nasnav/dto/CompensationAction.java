package com.nasnav.dto;

import com.nasnav.enumerations.CompensationActions;

import javax.validation.constraints.NotNull;

public record CompensationAction(
        @NotNull(message = "Action Name is required")
        CompensationActions name,
        String description
) {
}
