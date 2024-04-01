package com.nasnav.security.jwt;

import com.nasnav.service.SettingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.mock.mockito.MockBean;
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

@Slf4j
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG)
@Disabled("FIXME for CI")
class TokenControllerTest extends DockerPostgresDb {

    @MockBean
    SettingService settingService;

    @LocalServerPort
    static int port;

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

        TOKEN = WebClient.create()
                .post()
                .uri("http://localhost:" + port + "/nasnav/token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JwtLoginData.JwtWrapper.class)
                .block()
                .token();

    }

    @Test
    void testToken(@Autowired MockMvc mockMvc) throws Exception {

        Instant startInstant = Instant.now();
        POSTGRE_SQL_CONTAINER
                .execInContainer("psql", "-U", "nasnav", "-d", "nasnav", "-c", "SET session_replication_role = 'replica';", "-f", "/tmp/db-dump-data/" + currentDump);
        log.info("Docker dump duration is: {}", Duration.between(startInstant, Instant.now()));
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/nasnav/get-token")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + StringUtils.normalizeSpace(TOKEN))
                .accept(MediaType.APPLICATION_JSON);

        var mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Assertions.assertEquals("Success", mvcResult);
    }
}