package com.nasnav.service.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.nasnav.dao.*;
import com.nasnav.dto.request.notification.NotificationRequestDto;
import com.nasnav.dto.request.notification.SubscriptionRequestDto;
import com.nasnav.enumerations.TopicType;
import com.nasnav.persistence.*;
import com.nasnav.service.SecurityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService{
    private FirebaseApp firebaseApp;
    @Value("${app.firebase-config}")
    private String firebaseConfig;

    @Autowired
    private SecurityService securityService;
    @Autowired
    private EmployeeUserRepository employeeUserRepository;
    @Autowired
    private NotificationTopicsRepository notificationTopicsRepository;
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
                    .setCredentials(GoogleCredentials.fromStream(new ClassPathResource(firebaseConfig).getInputStream())).build();

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
    public void subscribeToTopic(SubscriptionRequestDto subscriptionRequestDto) {
        try {
            BaseUserEntity loggedInUser = securityService.getCurrentUser();
            UserTokensEntity userTokensEntity = userTokenRepository.getUserEntityByToken(loggedInUser.getAuthenticationToken());
            EmployeeUserEntity employeeUserEntity = userTokensEntity.getEmployeeUserEntity();

            if(employeeUserEntity != null){
                FirebaseMessaging.getInstance(firebaseApp).subscribeToTopic(Arrays.asList(subscriptionRequestDto.getToken()),
                        subscriptionRequestDto.getTopicName());

                if(userTokensEntity.getNotificationToken() == null || !userTokensEntity.getNotificationToken().equals(subscriptionRequestDto.getToken())){
                    userTokensEntity.setNotificationToken(subscriptionRequestDto.getToken());
                    userTokenRepository.save(userTokensEntity);
                }

                NotificationTopicsEntity notificationTopicsEntity = notificationTopicsRepository.getByTopic(subscriptionRequestDto.getTopicName());
                Set<NotificationTopicsEntity> notificationTopicsEntities = employeeUserEntity.getTopics();
                if(notificationTopicsEntities.add(notificationTopicsEntity)){
                    employeeUserEntity.setTopics(notificationTopicsEntities);
                    employeeUserRepository.save(employeeUserEntity);
                }
            }
        } catch (FirebaseMessagingException e) {
            log.error("Firebase subscribe to topic fail", e);
        }

    }

    @Override
    public void unsubscribeFromTopic(SubscriptionRequestDto subscriptionRequestDto) {
        try {
            BaseUserEntity loggedInUser = securityService.getCurrentUser();
            UserTokensEntity userTokensEntity = userTokenRepository.getUserEntityByToken(loggedInUser.getAuthenticationToken());
            EmployeeUserEntity employeeUserEntity = userTokensEntity.getEmployeeUserEntity();

            if(employeeUserEntity != null){
                if(userTokensEntity.getNotificationToken().equals(subscriptionRequestDto.getToken())){
                    NotificationTopicsEntity notificationTopicsEntity = notificationTopicsRepository.getByTopic(subscriptionRequestDto.getTopicName());
                    Set<NotificationTopicsEntity> notificationTopicsEntities = employeeUserEntity.getTopics();
                    if(notificationTopicsEntities.remove(notificationTopicsEntity)){
                        FirebaseMessaging.getInstance(firebaseApp).unsubscribeFromTopic(Arrays.asList(subscriptionRequestDto.getToken()),
                                subscriptionRequestDto.getTopicName());
                        employeeUserEntity.setTopics(notificationTopicsEntities);
                        employeeUserRepository.save(employeeUserEntity);
                    }
                }
            }
        } catch (FirebaseMessagingException e) {
            log.error("Firebase unsubscribe from topic fail", e);
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
    public void sendPnsToTopic(NotificationRequestDto notificationRequestDto) {
        Message message = Message.builder()
                .setTopic(notificationRequestDto.getTarget())
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
        Set<NotificationTopicsEntity> userTopics = employeeUserEntity.getTopics();
        if(employeeUserEntity != null){
            if(oldToken == null){
                try {
                    loginFirstTimeNotificationToken(employeeUserEntity,newToken);
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
                try {
                    for(NotificationTopicsEntity topic : userTopics){
                        FirebaseMessaging.getInstance(firebaseApp).unsubscribeFromTopic(Arrays.asList(oldToken),
                                topic.getTopic());

                        FirebaseMessaging.getInstance(firebaseApp).subscribeToTopic(Arrays.asList(newToken),
                                topic.getTopic());
                    }
                }
                catch (FirebaseMessagingException e){
                    log.error("Fail to send firebase notification", e);
                    return false;
                }

                List<UserTokensEntity> userTokensEntities = userTokenRepository.getAllByNotificationToken(oldToken);
                for(UserTokensEntity user : userTokensEntities){
                    user.setNotificationToken(newToken);
                }
                try {
                    userTokenRepository.saveAll(userTokensEntities);
                }
                catch (Exception e){
                    log.error("failed to save entity",e);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean createOrUpdateUserToken(String token, String authToken) {
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


    @Override
    public void refreshNotificationTopics() {
        List<OrganizationEntity> organizationEntities = organizationRepository.findAllOrganizations();
        List<ShopsEntity> shopsEntities = shopsRepository.findAllShops();
        List<NotificationTopicsEntity> notificationTopicsEntities = new ArrayList<>();

        for(OrganizationEntity org : organizationEntities){
            if(org.getTopic() == null){
                org.setTopic(checkTopicExistence(TopicType.ORG.getValue()+org.getId()));
                organizationRepository.save(org);
            }
            else if(!notificationTopicsRepository.existsByTopic(org.getTopic().getTopic())){
                NotificationTopicsEntity notificationTopicsEntity = new NotificationTopicsEntity();
                notificationTopicsEntity.setTopic(org.getTopic().getTopic());
                notificationTopicsEntities.add(notificationTopicsEntity);
            }
        }

        for(ShopsEntity shop : shopsEntities){
            if(shop.getTopic() == null){
                shop.setTopic(checkTopicExistence(TopicType.SHOP.getValue()+shop.getId()));
                shopsRepository.save(shop);
            }
            else if(!notificationTopicsRepository.existsByTopic(shop.getTopic().getTopic())){
                NotificationTopicsEntity notificationTopicsEntity = new NotificationTopicsEntity();
                notificationTopicsEntity.setTopic(shop.getTopic().getTopic());
                notificationTopicsEntities.add(notificationTopicsEntity);
            }
        }
        notificationTopicsRepository.saveAll(notificationTopicsEntities);
    }

    private NotificationTopicsEntity checkTopicExistence(String topicName) {
        NotificationTopicsEntity notificationTopicsEntity = new NotificationTopicsEntity();
        if(!notificationTopicsRepository.existsByTopic(topicName)){
            notificationTopicsEntity.setTopic(topicName);
            notificationTopicsEntity = notificationTopicsRepository.save(notificationTopicsEntity);
        }
        else {
            notificationTopicsEntity = notificationTopicsRepository.getByTopic(topicName);
        }
        return notificationTopicsEntity;
    }

    @Override
    public NotificationRequestDto getTopicByTopicName(String topicName) {
        NotificationTopicsEntity notificationTopicsEntity = notificationTopicsRepository.getByTopic(topicName);
        if(notificationTopicsEntity == null){
            return null;
        }
        else {
            return new NotificationRequestDto(notificationTopicsEntity.getTopic());
        }
    }

    private void loginFirstTimeNotificationToken(EmployeeUserEntity employeeUserEntity,String token) {
        List<String> topics = Arrays.asList(TopicType.ORG.getValue() +employeeUserEntity.getOrganizationId());
        if(employeeUserEntity.getShopId() != null){
            topics.add(TopicType.SHOP.getValue()+employeeUserEntity.getShopId());
        }
        try{
            for(String topic : topics){
                FirebaseMessaging.getInstance(firebaseApp).subscribeToTopic(Arrays.asList(token),
                        topic);

                NotificationTopicsEntity notificationTopicsEntity = notificationTopicsRepository.getByTopic(topic);
                Set<NotificationTopicsEntity> notificationTopicsEntities = employeeUserEntity.getTopics();
                if(notificationTopicsEntities.add(notificationTopicsEntity)){
                    employeeUserEntity.setTopics(notificationTopicsEntities);
                    employeeUserRepository.save(employeeUserEntity);
                }
            }
        }
        catch (FirebaseMessagingException e){
            log.error("Can't subscribe to topic: "+e);
        }
    }

}
