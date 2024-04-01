package com.nasnav.yeshtery.security.jwt;

import lombok.Builder;

import javax.annotation.Nonnull;

@Builder
public record JwtLoginData(
        String email,
        String password,
        boolean isEmployee,
        Long orgId) {

    public record JwtWrapper(@Nonnull String token) { }
}

