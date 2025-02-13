package com.nasnav.yeshtery.security.jwt;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class JwtUserDetailsServiceImplTest extends DockerPostgresDb {

    @Test
    @Transactional
    void loadUser(@Autowired JwtUserDetailsServiceImpl userDetailsService,
                  @Autowired PasswordEncoder passwordEncoder) throws Exception {

        POSTGRE_SQL_CONTAINER
                .execInContainer("psql", "-U", "nasnav", "-d", "nasnav", "-c", "SET session_replication_role = 'replica';", "-f", "/tmp/db-dump-data/" + currentDump);

        JwtLoginData jwtLoginData = getLoginData();

        JwtUserDetailsImpl userDetailsImpl = (JwtUserDetailsImpl) userDetailsService.loadUser(jwtLoginData);

        assertThat(userDetailsImpl.userEntity().getId()).isEqualTo(97);
        assertThat(userDetailsImpl.userEntity().getOrganizationId()).isEqualTo(6);
        assertThat(userDetailsImpl.userEntity().getShopId()).isZero();
        assertThat(userDetailsImpl.getAuthorities()).isNotEmpty();
        assertThat(userDetailsImpl.getUsername()).isEqualTo("mohamedghazi.pvt@gmail.com");
        assertThat(passwordEncoder.matches("password", userDetailsImpl.getPassword())).isTrue();
        assertThat(userDetailsImpl.isAccountNonExpired()).isTrue();
        assertThat(userDetailsImpl.isAccountNonLocked()).isTrue();
        assertThat(userDetailsImpl.isCredentialsNonExpired()).isTrue();
        assertThat(userDetailsImpl.isEnabled()).isTrue();

        log.info(userDetailsImpl.toString());
    }

    @Test
    void valideLoginData() {
    }

    @Test
    void validateLoginUser() {
    }

    @Test
    void isAccountLocked() {
    }

    @Test
    void isUserDeactivated() {
    }

    @Test
    void isEmployeeUserNeedActivation() {
    }
}