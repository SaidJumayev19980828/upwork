package com.nasnav.security.jwt;

import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * A controller for the token resource.
 *
 * @author Josh Cummings
 */
@RestController
public class TokenController {

    private final JwtOAuthService authService;
    private final JWKSet jwkSet;

    public TokenController(JwtOAuthService authService, JWKSet jwkSet) {
        this.authService = authService;
        this.jwkSet = jwkSet;
    }

    @PostMapping("/nasnav/token")
    public JwtLoginData.JwtWrapper token(@RequestBody JwtLoginData loginData) {
        return authService.tokenize(loginData);
    }

    @GetMapping("/nasnav/token/jwks.json")
    public Map<String, Object> keys() {
        return jwkSet.toJSONObject();
    }
}
