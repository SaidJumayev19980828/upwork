package com.nasnav;

import java.io.IOException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FireBaseConfig {
	@Bean
	@ConditionalOnProperty(name = "firebase-config")
	GoogleCredentials googleCredentials(AppConfig appConfig) {
		try {
			return GoogleCredentials.fromStream(new ClassPathResource(appConfig.firebaseConfig).getInputStream());
		} catch (IOException exception) {
			log.error("failed to read google credentials", exception);
		}
		return null;
	}

	@Bean
	@ConditionalOnBean(GoogleCredentials.class)
	FirebaseApp firebaseApp(GoogleCredentials googleCredentials) {
		FirebaseOptions options = FirebaseOptions.builder().setCredentials(googleCredentials).build();
		deleteDefaultInstanceIfExists();
		return FirebaseApp.initializeApp(options);
	}

	@Bean
	@ConditionalOnBean(FirebaseApp.class)
	FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
			return FirebaseMessaging.getInstance(firebaseApp);
	}

	private void deleteDefaultInstanceIfExists() {
		try {
			FirebaseApp existingInstance = FirebaseApp.getInstance();
			if (existingInstance != null) {
				existingInstance.delete();
			}
		} catch(IllegalStateException ex) {
			// the try is only to handle the exception if instance doesn't exist
		}
	}
}
