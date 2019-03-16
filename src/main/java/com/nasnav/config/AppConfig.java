package com.nasnav.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
// create data source from database.properties file
@PropertySource("classpath:config/database.properties")
public class AppConfig {
}
