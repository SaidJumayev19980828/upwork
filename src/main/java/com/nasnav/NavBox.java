package com.nasnav;

import java.io.IOException;
import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

@SpringBootApplication
public class NavBox
{

    public static void main(String[] args) throws IOException
    {
        Resource resource = new ClassPathResource("application.properties");
        Properties baseProperties = PropertiesLoaderUtils.loadProperties(resource);
        Resource database = new ClassPathResource("database.properties");
        Properties databaseProperties = PropertiesLoaderUtils.loadProperties(database);
        databaseProperties.forEach((key, value) -> System.setProperty((String)key, (String)value));

        
        //---------------------------------------------------------------
        //set multipart properties here until we can modify application.properties on the server
        Properties properties = new Properties();
        properties.put("spring.servlet.multipart.max-file-size", -1);
        properties.put("spring.servlet.multipart.max-request-size", -1);
        properties.put("server.compression.enabled", true);
        properties.put("server.compression.mime-types", "text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json");
        properties.put("server.compression.min-response-size", 1024);
        
        //Google OAuth2 properties
        properties.put("spring.security.oauth2.client.registration.google.clientId", "149229237117-75f15qq1meqoki3ekmt4q50vm5dj82rs.apps.googleusercontent.com");
        properties.put("spring.security.oauth2.client.registration.google.clientSecret", "WlmPNZcMVDhj5tamnGqodWwr");
        properties.put("spring.security.oauth2.client.registration.google.redirectUriTemplate", "{baseUrl}/oauth2/callback/{registrationId}");
        properties.put("spring.security.oauth2.client.registration.google.scope", "email,profile,openid");
        //---------------------------------------------------------------
        
        SpringApplication application = new SpringApplication(NavBox.class);
        application.setDefaultProperties(properties);
        application.run(args);
        //---------------------------------------------------------------
//        SpringApplication.run(NavBox.class, args);
    }
}

