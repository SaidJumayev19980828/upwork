package com.nasnav;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Properties;

@Configuration
public class AppConfig {

    @PostConstruct
    public void init() throws IOException {
        Resource resource = new ClassPathResource("mail.properties");
        Properties mailProperties = PropertiesLoaderUtils.loadProperties(resource);
        mailProperties.forEach((key, value) -> System.setProperty((String)key, (String)value));
    }

    /**
     * Register password encoder bean
     *
     * @return PasswordEncoder object
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Register Mail Sender bean
     *
     * @return JavaMailSender
     */
    @Bean
    public JavaMailSender mailSender() {
        return new NasNavMailSender();
    }

}
