package com.nasnav.security.jwt;


import com.nasnav.NavBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.LogManager;

@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
@DirtiesContext
@Slf4j
public class DockerPostgresDb {
    /**
     * This is a constant variable representing the name of the current database dump file.
     */
    static final String currentDump = "pg-dump.sql";

    static {
        // Postgres JDBC driver uses JUL; disable it to avoid annoying, irrelevant, stderr logs during connection testing
        LogManager.getLogManager().getLogger("").setLevel(Level.OFF);
    }

    @Container
    static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.2-alpine"))
                    .withCopyFileToContainer(MountableFile.forClasspathResource("db-dump-data/" + currentDump), "/tmp/db-dump-data/" + currentDump)
                    .withDatabaseName("nasnav")
                    .withUsername("nasnav")
                    .withPassword("nasnav")
                    .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\s", 1))
                    .withStartupTimeout(Duration.ofSeconds(30))
            //.withLogConsumer(new Slf4jLogConsumer(log))
            ;

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", POSTGRE_SQL_CONTAINER::getJdbcUrl);
        dynamicPropertyRegistry.add("spring.datasource.username", POSTGRE_SQL_CONTAINER::getUsername);
        dynamicPropertyRegistry.add("spring.datasource.password", POSTGRE_SQL_CONTAINER::getPassword);
        dynamicPropertyRegistry.add("spring.datasource.driver-class-name", POSTGRE_SQL_CONTAINER::getDriverClassName);
    }

    static JwtLoginData getLoginData() {
        return JwtLoginData.builder()
                .email("mohamedghazi.pvt@gmail.com")
                .orgId(6L)
                .isEmployee(false)
                .password("password")
                .build();
    }

}
