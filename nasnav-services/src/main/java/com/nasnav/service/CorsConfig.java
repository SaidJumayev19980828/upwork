package com.nasnav.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;


@Configuration
//@EnableWebMvc
public class CorsConfig implements WebMvcConfigurer {
    @Value("${allowed.origins}")
    private String allowedOrigins;
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedHeaders("*");
    }


}
