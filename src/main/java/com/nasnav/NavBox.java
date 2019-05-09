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

        SpringApplication.run(NavBox.class, args);
    }
}

