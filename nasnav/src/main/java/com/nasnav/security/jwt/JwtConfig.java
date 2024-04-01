
package com.nasnav.security.jwt;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
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
    JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(this.key)
                .privateKey(this.priv)
                .build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }
}
