package com.nasnav.yeshtery.security.jwt;

import com.nasnav.persistence.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class JwtDaoAuthenticationProviderTest extends DockerPostgresDb {

    @Test
    @Transactional
    void authenticate_test(@Autowired JwtDaoAuthenticationProvider provider) throws Exception {

        POSTGRE_SQL_CONTAINER
                .execInContainer("psql", "-U", "nasnav", "-d", "nasnav", "-c", "SET session_replication_role = 'replica';", "-f", "/tmp/db-dump-data/" + currentDump);

        JwtLoginData loginData = getLoginData();
        YeshteryAuthenticationToken token = new YeshteryAuthenticationToken(loginData);
        Authentication authenticatedUser = provider.authenticate(token);

        assertThat(authenticatedUser)
                .as("The token must be of type UsernamePasswordAuthenticationToken")
                .isExactlyInstanceOf(UsernamePasswordAuthenticationToken.class);

        assertThat(authenticatedUser.getPrincipal())
                .as("The principal must be of type JwtUserDetailsImpl")
                .isExactlyInstanceOf(JwtUserDetailsImpl.class);

        JwtUserDetailsImpl jwtUserDetails = (JwtUserDetailsImpl) authenticatedUser.getPrincipal();

        UserEntity userEntity = new UserEntity();
        userEntity.setId(97L);
        userEntity.setYeshteryUserId(33L);
        userEntity.setName("Mohamed Ghazi");

        JwtUserDetailsImpl expectedUserDetails = JwtUserDetailsImpl.builder()
                .userEntity(userEntity)
                .authorities(List.of(new SimpleGrantedAuthority("CUSTOMER")))
                .build();

        assertThat(jwtUserDetails)
                .as("The current principal must be equals to expectedUser")
                .isEqualTo(expectedUserDetails);
    }

    @Test
    void yeshtery_authentication_token_type_test(@Autowired JwtDaoAuthenticationProvider provider) {
        JwtLoginData loginData = getLoginData();
        YeshteryAuthenticationToken token = new YeshteryAuthenticationToken(loginData);

        assertThat(provider.supports(token.getClass()))
                .as("The provider must support YeshteryAuthenticationToken type")
                .isTrue();
    }

    @Test
    void does_not_supports_other_type_test(@Autowired JwtDaoAuthenticationProvider provider) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("user1", "password1");

        assertThat(provider.supports(token.getClass()))
                .as("The provider should not support UsernamePasswordAuthenticationToken type")
                .isFalse();
    }
}