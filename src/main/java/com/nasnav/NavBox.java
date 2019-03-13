package com.nasnav;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

@SpringBootApplication
public class NavBox
{

    public static void main(String[] args) throws IOException
    {
        Resource resource = new ClassPathResource("application.properties");
        Properties baseProperties = PropertiesLoaderUtils.loadProperties(resource);
        Resource databasePropertiesFile = new FileSystemResource(baseProperties.getProperty("database.properties"));
        Properties databaseProperties = PropertiesLoaderUtils.loadProperties(databasePropertiesFile);
        databaseProperties.forEach((key, value) -> System.setProperty((String)key, (String)value));

        SpringApplication.run(NavBox.class, args);
    }
}

