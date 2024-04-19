package com.nasnav.yeshtery.security.jwt;

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

    @PostMapping("/v1/yeshtery/token")
    public JwtResponse token(@RequestBody JwtLoginData loginData) {
        return authService.tokenize(loginData);
    }

    @GetMapping("/v1/yeshtery/token/jwks.json")
    public Map<String, Object> keys() {
        return this.jwkSet.toJSONObject();
    }
}
