package com.nasnav.yeshtery.security.jwt;

import javax.annotation.Nonnull;

public record JwtResponse(@Nonnull String token,@Nonnull String refresh, UserInfo userInfo) { }
