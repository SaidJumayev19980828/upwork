package com.nasnav.security.jwt;

import lombok.Builder;

import javax.annotation.Nonnull;
import java.util.Set;

public record JwtResponse(@Nonnull String token, UserInfo userInfo) { }

@Builder
record UserInfo(
        Long id,
        String name,
        String email,
        Set<String> roles,
        String imageUrl,
        Long organizationId,
        boolean isEmployee,
        Long shopId) { }
