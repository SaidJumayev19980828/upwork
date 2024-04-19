package com.nasnav.security.jwt;

import com.nasnav.persistence.BaseUserEntity;
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
import java.util.Set;
import java.util.stream.Collectors;

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

    JwtResponse tokenize(JwtUserDetailsImpl userDetails) {
        Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiry = issuedAt.plus(1, ChronoUnit.HOURS);

        UserInfo userInfo = buildUserInfo(userDetails);

        JwtClaimsSet claims = createClaimsSet(issuedAt, expiry, userInfo);
        JwsHeader jwsHeader = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(JwtConfig.JWT_KID)
                .build();
        JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters.from(jwsHeader, claims);
        String tokenValue = this.encoder.encode(jwtEncoderParameters).getTokenValue();
        log.info("The token for user {} and organization {} is: {}", userDetails.getUsername(), userDetails.userEntity().getOrganizationId(), tokenValue);

        return new JwtResponse(tokenValue, userInfo);
    }

    static JwtClaimsSet createClaimsSet(Instant issuedAt, Instant expiry, UserInfo userInfo) {
        return JwtClaimsSet.builder()
                .issuer("com.nasnav")
                .issuedAt(issuedAt)
                .notBefore(issuedAt)
                .expiresAt(expiry)
                .subject(userInfo.email())
                .claim(JwtOAuthService.ORGANIZATION_ID_CLAIM, userInfo.organizationId())
                .claim(JwtOAuthService.USER_ID_CLAIM, userInfo.id())
                .claim(JwtOAuthService.EMPLYEE_CLAIM, userInfo.isEmployee())
                .claim("roles", userInfo.roles())
                .build();
    }

    static UserInfo buildUserInfo(JwtUserDetailsImpl userDetails) {
        Set<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

        BaseUserEntity userEntity = userDetails.userEntity();

        return UserInfo.builder()
                .name(userEntity.getName())
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .roles(roles)
                .imageUrl(userEntity.getImage())
                .organizationId(userEntity.getOrganizationId())
                .shopId(userEntity.getShopId())
                .isEmployee(userEntity.isEmployee())
                .build();
    }

    @Override
    public JwtResponse tokenize(JwtLoginData loginData) {

        JwtUserDetailsImpl userDetails = authenticate(loginData);
        return tokenize(userDetails);
    }
}
