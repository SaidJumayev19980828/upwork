package com.nasnav.yeshtery.security.jwt;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class JwtOAuthServiceImplTest extends DockerPostgresDb {

    @Test
    void tokenize(@Autowired JwtOAuthServiceImpl authService) throws Exception {
        POSTGRE_SQL_CONTAINER
                .execInContainer("psql", "-U", "nasnav", "-d", "nasnav", "-c", "SET session_replication_role = 'replica';", "-f", "/tmp/db-dump-data/" + currentDump);

        JwtLoginData loginData = getLoginData();
        JwtResponse tokenized = authService.tokenize(loginData);

        assertThat(tokenized)
                .isNotNull();
    }
}