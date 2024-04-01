package com.nasnav.security.jwt;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * A controller for the token resource.
 *
 * @author Josh Cummings
 */
@RestController
public class TokenController {

    private final JwtOAuthService authService;

    public TokenController(JwtOAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/nasnav/token")
    public JwtLoginData.JwtWrapper token(@RequestBody JwtLoginData loginData) {
        return authService.tokenize(loginData);
    }

}
