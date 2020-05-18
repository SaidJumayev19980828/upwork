package com.nasnav;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.common.SwaggerPluginSupport;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;

import static springfox.documentation.swagger.common.SwaggerPluginSupport.pluginDoesApply;

@Configuration
@EnableSwagger2
public class Swagger {

    @Bean
    public Docket apiDoc() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .paths(PathSelectors.any())
                .build()
        ;
    }


    @Component
    @Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER + 1000)
    public class SwaggerCustomParameterBuilderPlugin implements ParameterBuilderPlugin {

        @Override
        public void apply(ParameterContext context) {
            if (isCookieValue(context)) {
                context.parameterBuilder().parameterType("cookie");
            }
        }

        private boolean isCookieValue(ParameterContext context) {
            return context
                    .resolvedMethodParameter()
                    .getAnnotations()
                    .stream()
                    .anyMatch(annotation -> annotation.annotationType() == CookieValue.class);
        }

        @Override
        public boolean supports(DocumentationType documentationType) {
            return pluginDoesApply(documentationType);
        }

    }
}
