package com.nasnav;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.nasnav.enumerations.converters.LowerCaseToConvertedImageTypesConverter;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new LowerCaseToConvertedImageTypesConverter());
    }
}
