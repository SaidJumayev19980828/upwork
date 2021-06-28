package com.nasnav.yeshtery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.IOException;
import java.util.Properties;

@SpringBootApplication(exclude = {
        ElasticsearchDataAutoConfiguration.class,
        ElasticsearchRestClientAutoConfiguration.class})
@EnableCaching
@EnableJpaRepositories(basePackages = {"com.nasnav.yeshtery.dao"})
@ComponentScan({"com.nasnav"})
public class Yeshtery{

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
        
        //---------------------------------------------------------------
        
        SpringApplication application = new SpringApplication(Yeshtery.class);
        application.setDefaultProperties(properties);
        application.run(args);
        //---------------------------------------------------------------
    }
}

