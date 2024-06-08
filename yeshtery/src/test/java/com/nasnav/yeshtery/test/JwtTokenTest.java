package com.nasnav.yeshtery.test;

import com.nasnav.enumerations.Roles;
import com.nasnav.yeshtery.security.jwt.JwtLoginData;
import com.nasnav.yeshtery.security.jwt.JwtOAuthService;
import com.nasnav.yeshtery.security.jwt.JwtResponse;
import com.nasnav.yeshtery.security.jwt.TokenController;
import com.nasnav.yeshtery.security.jwt.UserInfo;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;
import com.nimbusds.jose.jwk.JWKSet;
import net.jcip.annotations.NotThreadSafe;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@AutoConfigureWebTestClient
@NotThreadSafe
@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = { "/sql/Event_Test_Data.sql" })
@Sql(executionPhase = AFTER_TEST_METHOD, scripts = { "/sql/database_cleanup.sql" })
class JwtTokenTest extends AbstractTestWithTempBaseDir {

        @Test
        void test_valid_login_data_returns_jwt_response_with_token() {
            JwtOAuthService authService = mock(JwtOAuthService.class);
            JWKSet jwkSet = mock(JWKSet.class);
            TokenController tokenController = new TokenController(authService, jwkSet);

            JwtLoginData loginData = JwtLoginData.builder()
                    .email("mohamedghazi.pvt@gmail.com")
                    .password("password")
                    .isEmployee(false)
                    .orgId(6L)
                    .build();

            UserInfo userInformation = new UserInfo(1L, "mohamedghazi.pvt@gmail.com", "password", Set.of(Roles.CUSTOMER.getValue()), null,99001L,false,501L);

            JwtResponse expectedResponse = new JwtResponse("token", "refresh",userInformation );
            when(authService.tokenize(loginData)).thenReturn(expectedResponse);

            JwtResponse actualResponse = tokenController.token(loginData);

            assertEquals(expectedResponse, actualResponse);
        }

        // Invalid login data returns an appropriate error response
        @Test
        void test_invalid_login_data_returns_error_response() {
            JwtOAuthService authService = mock(JwtOAuthService.class);
            JWKSet jwkSet = mock(JWKSet.class);
            TokenController tokenController = new TokenController(authService, jwkSet);

            JwtLoginData loginData = JwtLoginData.builder()
                    .email("invalid@example.com")
                    .password("wrongpassword")
                    .isEmployee(false)
                    .orgId(6L)
                    .build();

            when(authService.tokenize(loginData)).thenThrow(new RuntimeException("Invalid credentials"));

            Exception exception = assertThrows(RuntimeException.class, () -> {
                tokenController.token(loginData);
            });

            assertEquals("Invalid credentials", exception.getMessage());
        }

    }
