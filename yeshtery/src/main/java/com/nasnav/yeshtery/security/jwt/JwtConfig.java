
package com.nasnav.yeshtery.security.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;

/**
 * Class for configuring JWT (JSON Web Token) in Spring Security.
 */
@Configuration
@EnableWebSecurity
public class JwtConfig {

    public static final String JWT_KID = "meetusvr-jwt-key";

    @Value("classpath:jwt/app.pub")
    RSAPublicKey key;

    @Value("classpath:jwt/app.key")
    RSAPrivateKey priv;


    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider(JwtAuthenticationConverter jwtAuthenticationConverter) {
        JwtAuthenticationProvider jwtAuthProvider = new JwtAuthenticationProvider(jwtDecoder());
        jwtAuthProvider.setJwtAuthenticationConverter(jwtAuthenticationConverter);
        return jwtAuthProvider;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    JwtDecoder jwtDecoder() {
        var jwtDecoder = NimbusJwtDecoder
                .withPublicKey(this.key)
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();

        var withClockSkew = new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator(Duration.ofSeconds(60)));

        jwtDecoder.setJwtValidator(withClockSkew);

        return jwtDecoder;
    }

    @Bean
    JwtEncoder jwtEncoder(JWKSet jwkSet) {
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(jwkSet);
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public JWKSet jwkSet() {
        RSAKey.Builder builder = new RSAKey
                .Builder(this.key)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .privateKey(this.priv)
                .keyID(JWT_KID);

        return new JWKSet(builder.build());
    }
}
