package com.nasnav.security.jwt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record JwtLoginData(
        @Schema(defaultValue = "mohamedghazi.pvt@gmail.com")
        String email,
        @Schema(name = "password", defaultValue = "password")
        String password,
        @Schema(defaultValue = "false")
        boolean isEmployee,
        @Schema(defaultValue = "6")
        Long orgId) {

}

