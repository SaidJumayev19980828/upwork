package com.nasnav.yeshtery.security.jwt;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class YeshteryAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private final JwtLoginData loginData;

    public YeshteryAuthenticationToken(JwtLoginData loginData) {
        super(loginData.email(), loginData.password());

        this.loginData = loginData;
    }

    public JwtLoginData getLoginData() {
        return loginData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        YeshteryAuthenticationToken that = (YeshteryAuthenticationToken) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(loginData, that.loginData)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(loginData).toHashCode();
    }
}
