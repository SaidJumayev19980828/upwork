package com.nasnav;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


@Configuration
@Slf4j
@Profile("!test")
@RequiredArgsConstructor
public class FrontEndSettingConfig {
   private final AppConfig appConfig;
    private final static String KEY_NAME_VALIDATION_PATTERN = "^([a-zA-Z0-9_](\\.[a-zA-Z0-9_])*)*";
    @Bean("frontendProps")
    public Properties frontendProps() {
      return loadPropertiesFile();
    }

    private Properties loadPropertiesFile() {
        final var properties = new Properties();

        try (final var propertiesFileAsInputStream =  new FileInputStream(appConfig.filePath)) {

             properties.load(propertiesFileAsInputStream);

            final var isNotMatchingValidationPattern = properties.entrySet().stream()
                    .anyMatch(entry -> entry.getKey() instanceof String && !((String) entry.getKey()).matches(KEY_NAME_VALIDATION_PATTERN));
            if(isNotMatchingValidationPattern){
                log.warn("File doesn't match the schema");
                return new Properties();
            }
        } catch (IOException e) {
            log.warn("File Not Found");
           return properties;
        }
       return properties;
    }
}
