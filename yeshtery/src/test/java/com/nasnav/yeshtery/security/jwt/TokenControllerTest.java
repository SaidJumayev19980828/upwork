package com.nasnav.yeshtery.security.jwt;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.Instant;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@AutoConfigureMockMvc()
@Disabled("FIXME for CI")
class TokenControllerTest extends DockerPostgresDb {

    private static final Logger log = LoggerFactory.getLogger(TokenControllerTest.class);
    static String TOKEN;

    @BeforeAll
    static void setupToken(@LocalServerPort int port) throws Exception {

        POSTGRE_SQL_CONTAINER
                .execInContainer("psql", "-U", "nasnav", "-d", "nasnav", "-c", "SET session_replication_role = 'replica';", "-f", "/tmp/db-dump-data/" + currentDump);

        String requestBody = """
                {
                    "email":"mohamedghazi.pvt@gmail.com",
                    "password":"password",
                    "isEmployee":false,
                    "orgId":6
                }
                """;

        JwtResponse jwtResponse = WebClient.create()
                .post()
                .uri("http://localhost:" + port + "/v1/yeshtery/token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JwtResponse.class)
                .log()
                .block();

        log.info(jwtResponse.toString());
        TOKEN = jwtResponse.token();

        log.info("Token: {}", TOKEN);

    }

    @Test
    void testToken(@Autowired MockMvc mockMvc) throws Exception {

        Instant startInstant = Instant.now();
        POSTGRE_SQL_CONTAINER
                .execInContainer("psql", "-U", "nasnav", "-d", "nasnav", "-c", "SET session_replication_role = 'replica';", "-f", "/tmp/db-dump-data/" + currentDump);
        log.info("Docker dump duration is: {}", Duration.between(startInstant, Instant.now()));
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/v1/user/info")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + StringUtils.normalizeSpace(TOKEN))
                .accept(MediaType.APPLICATION_JSON);

        String mvcExpectedResult = """
                {
                  "id": 97,
                  "name": "Mohamed Ghazi",
                  "email": "mohamedghazi.pvt@gmail.com",
                  "addresses": [],
                  "organization_id": 6,
                  "shop_id": 0,
                  "roles": [
                    "CUSTOMER"
                  ],
                  "status": "ACTIVATED",
                  "creation_date": "2023-10-31T10:04:14.231893",
                  "allow_reward": false,
                  "referral": "97",
                  "is_influencer": false,
                  "last_login": "2024-03-15T14:07:09.989012"
                }
                """;

        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(mvcExpectedResult))
                .andDo(print());

    }
}