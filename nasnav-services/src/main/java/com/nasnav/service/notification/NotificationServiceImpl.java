package com.nasnav.service.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.nasnav.AppConfig;
import com.nasnav.dao.*;
import com.nasnav.dto.request.notification.NotificationRequestDto;
import com.nasnav.persistence.*;
import com.nasnav.service.SecurityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {
    // TODO: Error handling (throw proper exceptions)
    private FirebaseApp firebaseApp;
    @Autowired
    private AppConfig appConfig;

    @Lazy
    @Autowired
    private SecurityService securityService;
    @Autowired
    private EmployeeUserRepository employeeUserRepository;
    @Autowired
    private UserTokenRepository userTokenRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private ShopsRepository shopsRepository;

    @PostConstruct
    private void initialize() {
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
    public void sendPnsToDevice(NotificationRequestDto notificationRequestDto) {
        Message message = Message.builder()
                .setToken(notificationRequestDto.getTarget())
                .setNotification(new Notification(notificationRequestDto.getTitle(), notificationRequestDto.getBody()))
                .putData("content", notificationRequestDto.getTitle())
                .putData("body", notificationRequestDto.getBody())
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.error("Fail to send firebase notification", e);
        }

    }


    @Override
    public boolean createOrUpdateEmployeeToken(String newToken, String authToken) {
        UserTokensEntity userTokensEntity = userTokenRepository.getUserEntityByToken(authToken);
        EmployeeUserEntity employeeUserEntity = userTokensEntity.getEmployeeUserEntity();
        String oldToken = userTokensEntity.getNotificationToken();
//        Set<NotificationTopicEntity> userTopics = employeeUserEntity.getNotificationTopics();
        if(oldToken == null){
            try {
                userTokensEntity.setNotificationToken(newToken);
                userTokenRepository.save(userTokensEntity);
                return true;
            }
            catch (Exception e){
                log.error("failed to save entity",e);
                return false;
            }
        }
        else if (!oldToken.equals(newToken)){
//            try {
//                for(NotificationTopicEntity topic : userTopics){
//                    FirebaseMessaging.getInstance(firebaseApp).unsubscribeFromTopic(Arrays.asList(oldToken),
//                            topic.getTopic());
//
//                    FirebaseMessaging.getInstance(firebaseApp).subscribeToTopic(Arrays.asList(newToken),
//                            topic.getTopic());
//                }
//            }
//            catch (FirebaseMessagingException e){
//                log.error("Fail to send firebase notification", e);
//                return false;
//            }

            userTokensEntity.setNotificationToken(newToken);
            try {
                userTokenRepository.save(userTokensEntity);
            }
            catch (Exception e){
                log.error("failed to save entity",e);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean createOrUpdateCustomerToken(String token, String authToken) {
        UserTokensEntity userTokensEntity = userTokenRepository.getUserEntityByToken(authToken);
        userTokensEntity.setNotificationToken(token);
        try {
            userTokenRepository.save(userTokensEntity);
        }
        catch (Exception e){
            log.error("failed to save entity" , e);
            return false;
        }
        return true;
    }

}
