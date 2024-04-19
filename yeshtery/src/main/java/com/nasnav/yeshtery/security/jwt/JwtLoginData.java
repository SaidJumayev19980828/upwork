package com.nasnav.yeshtery.security.jwt;

import lombok.Builder;

@Builder
public record JwtLoginData(
        String email,
        String password,
        boolean isEmployee,
        Long orgId) {
}

