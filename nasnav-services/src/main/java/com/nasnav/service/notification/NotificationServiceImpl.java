package com.nasnav.service.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import com.nasnav.AppConfig;
import com.nasnav.dao.*;
import com.nasnav.dto.request.notification.NotificationRequestDto;
import com.nasnav.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private FirebaseApp firebaseApp;
    @Autowired
    private AppConfig appConfig;

    @PostConstruct
    public void initialize() {
        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(new ClassPathResource(appConfig.firebaseConfig).getInputStream())).build();

            if (FirebaseApp.getApps().isEmpty()) {
                this.firebaseApp = FirebaseApp.initializeApp(options);
            } else {
                this.firebaseApp = FirebaseApp.getInstance();
            }
        } catch (IOException e) {
            log.error("Create FirebaseApp Error", e);
        }
    }
    @Override
    public void sendMessage(NotificationRequestDto notifications) {

        Message message = Message.builder()
                .setToken(notifications.getTarget())
                .setNotification(new Notification(notifications.getTitle(), notifications.getBody()))
                .putData("content", notifications.getTitle())
                .putData("body", notifications.getBody())
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.error("Fail to send firebase notification", e);
        }

    }


}
