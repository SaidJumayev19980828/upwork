package com.nasnav.security.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class JwtOAuthServiceImpl implements JwtOAuthService {

    private final JwtDaoAuthenticationProvider jwtAuthProvider;
    private final JwtEncoder encoder;

    public JwtOAuthServiceImpl(JwtDaoAuthenticationProvider jwtAuthProvider, JwtEncoder encoder) {
        this.jwtAuthProvider = jwtAuthProvider;
        this.encoder = encoder;
    }

    JwtUserDetailsImpl authenticate(JwtLoginData loginData) {
        log.info("Authentication attempt fro user {} with organisation {}", loginData.email(), loginData.orgId());
        YeshteryAuthenticationToken authenticationToken = new YeshteryAuthenticationToken(loginData);
        Authentication authenticatedUser = jwtAuthProvider.authenticate(authenticationToken);

        if (authenticatedUser == null || !authenticatedUser.isAuthenticated()) {
            throw new BadCredentialsException("Invalid username or password");
        }
        return (JwtUserDetailsImpl) authenticatedUser.getPrincipal();
    }

    JwtLoginData.JwtWrapper tokenize(JwtUserDetailsImpl userDetails) {
        Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiry = issuedAt.plus(1, ChronoUnit.HOURS);

        var scopes = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("com.nasnav")
                .issuedAt(issuedAt)
                .notBefore(issuedAt)
                .expiresAt(expiry)
                .subject(userDetails.getUsername())
                .claim(JwtOAuthService.ORGANIZATION_ID_CLAIM, userDetails.orgId())
                .claim(JwtOAuthService.USER_ID_CLAIM, userDetails.id())
                .claim(JwtOAuthService.EMPLYEE_CLAIM, userDetails.isEmployee())
                .claim("roles", scopes)
                .build();
        // @formatter:on
        JwsHeader jwsHeader = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(JwtConfig.JWT_KID)
                .build();
        JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters.from(jwsHeader, claims);
        String tokenValue = this.encoder.encode(jwtEncoderParameters).getTokenValue();
        log.info("The token for user {} and organization {} is: {}", userDetails.getUsername(), userDetails.orgId(), tokenValue);

        return new JwtLoginData.JwtWrapper(tokenValue);
    }

    @Override
    public JwtLoginData.JwtWrapper tokenize(JwtLoginData loginData) {

        JwtUserDetailsImpl userDetails = authenticate(loginData);
        return tokenize(userDetails);
    }
}
