package com.nasnav.test;

import com.nasnav.enumerations.Roles;
import com.nasnav.security.jwt.JwtLoginData;
import com.nasnav.security.jwt.JwtOAuthService;
import com.nasnav.security.jwt.JwtResponse;
import com.nasnav.security.jwt.TokenController;
import com.nasnav.security.jwt.UserInfo;
import com.nimbusds.jose.jwk.JWKSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)

class JwtTokenTest {
    @Autowired
    private TestRestTemplate template;

    @Mock
    private JwtOAuthService authService;
    @Mock
    private JWKSet jwkSet;



        @Test
        void test_valid_login_data_returns_jwt_response_with_token() {
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
