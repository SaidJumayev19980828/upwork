package com.nasnav.security.jwt;

import lombok.Builder;

import java.util.Set;

@Builder
public record UserInfo(
        Long id,
        String name,
        String email,
        Set<String> roles,
        String imageUrl,
        Long organizationId,
        boolean isEmployee,
        Long shopId) {
}
