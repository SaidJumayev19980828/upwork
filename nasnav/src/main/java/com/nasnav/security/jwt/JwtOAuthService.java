package com.nasnav.security.jwt;

public interface JwtOAuthService {

    String ORGANIZATION_ID_CLAIM = "orgId";
    String USER_ID_CLAIM = "userId";
    String EMPLYEE_CLAIM = "employee";

    JwtResponse tokenize(JwtLoginData loginData);

}
