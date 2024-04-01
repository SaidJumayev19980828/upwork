package com.nasnav.yeshtery.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
//@formatter:off
@OpenAPIDefinition(
        info = @Info(title = "YESHTERY API",
                version = "${api.version}",
                summary = "",
                contact = @Contact(name = "todo", email = "todo", url = "todo"),
                license = @License(name = "todo", url = "todo"),
                termsOfService = "${tos.uri}",
                description = "Yeshtery Open API description (TO BE COMPLETED)."),
        security = {
                @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = YeshteryOpenAPIConfiguration.BEARER_TOKEN), // JWT Ref
                @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = YeshteryOpenAPIConfiguration.USER_TOKEN) // Opaque Token Ref
        }
)
//@formatter:on
// JWT token configuration
@SecurityScheme(
        name = YeshteryOpenAPIConfiguration.BEARER_TOKEN,
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        in = SecuritySchemeIn.HEADER,
        description = " Provide the JWT token. JWT token can be obtained from the Login API.")
// Legacy opaque token configuration.
@SecurityScheme(
        name = YeshteryOpenAPIConfiguration.USER_TOKEN,
        type = SecuritySchemeType.APIKEY,
        paramName = YeshteryOpenAPIConfiguration.USER_TOKEN_HEADER_NAME,
        in = SecuritySchemeIn.HEADER,
        description = "Provide legacy User Token.")
public class YeshteryOpenAPIConfiguration {
    static final String BEARER_TOKEN = "JWT BearerToken";
    static final String USER_TOKEN = "User Token";
    static final String USER_TOKEN_HEADER_NAME = "User-Token";
}