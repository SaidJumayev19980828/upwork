package com.nasnav.yeshtery;

import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.nasnav.AppConfig;
import com.nasnav.dao.SchedulerTaskRepository;
import com.nasnav.persistence.SchedulerTaskEntity;
import com.nasnav.service.scheduler.ScheduleTaskHelper;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Properties;

@SpringBootApplication(exclude = {
        ElasticsearchDataAutoConfiguration.class,
        ElasticsearchRestClientAutoConfiguration.class})
@EnableCaching
@EnableScheduling
@EnableJpaRepositories(basePackages = {"com.nasnav.yeshtery.dao"})
@EntityScan("com.nansav.persistence")
@ComponentScan({"com.nasnav"})
@EnableAsync
public class Yeshtery{
    @Autowired
    private SchedulerTaskRepository schedulerTaskRepository;

    @Autowired
    private ScheduleTaskHelper scheduleTaskHelper;

    @Bean
    public void runScheduleTask() {
        this.scheduleTaskHelper.runScheduleTask();
        this.scheduleTaskHelper.runInterestReminderMail();
    }


    @Bean

    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public AppConfig initAppConfig() {
        return new AppConfig(true);
    }

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
        properties.put("springdoc.swagger-ui.docExpansion", "none");
        properties.put("springdoc.writer-with-order-by-keys", true);
        properties.put("server.error.path", "/error");
        properties.put("spring.main.allow-circular-references", true);
        //---------------------------------------------------------------
        
        SpringApplication application = new SpringApplication(Yeshtery.class);
        application.setDefaultProperties(properties);
        application.run(args);
        //---------------------------------------------------------------

    }
}

