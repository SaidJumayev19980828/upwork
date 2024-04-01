package com.nasnav.yeshtery.security.jwt;

import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Builder
public record JwtUserDetailsImpl(Long id,
                                 String userName,
                                 String password,
                                 String email,
                                 Long orgId,
                                 Long shoId,
                                 boolean isEmployee,
                                 List<? extends GrantedAuthority> authorities) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Builds a JwtUserDetailsImpl object using the provided information.
     *
     * @param userEntity  the base user entity
     * @param shopId      the shop ID
     * @param authorities the list of granted authorities
     * @return a JwtUserDetailsImpl object with the specified details
     */
    public static JwtUserDetailsImpl buildJwtUserDetails(BaseUserEntity userEntity, Long shopId, List<SimpleGrantedAuthority> authorities) {

        boolean isEmployee = EmployeeUserEntity.class.isAssignableFrom(userEntity.getClass());
        return JwtUserDetailsImpl.builder()
                .id(userEntity.getId())
                .userName(userEntity.getName())
                .password(userEntity.getEncryptedPassword())
                .email(userEntity.getEmail())
                .orgId(userEntity.getOrganizationId())
                .shoId(shopId)
                .isEmployee(isEmployee)
                .authorities(authorities)
                .build();
    }
}
