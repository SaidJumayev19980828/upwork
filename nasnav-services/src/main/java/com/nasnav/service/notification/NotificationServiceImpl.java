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
import com.nasnav.dto.request.notification.SubscriptionRequestDto;
import com.nasnav.enumerations.TopicType;
import com.nasnav.persistence.*;
import com.nasnav.service.SecurityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {
    // TODO: Error handling (throw proper exceptions)
    private FirebaseApp firebaseApp;
    @Autowired
    private AppConfig appConfig;

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

                NotificationTopicEntity notificationTopicsEntity = notificationTopicsRepository.getByTopic(subscriptionRequestDto.getTopicName());
                Set<NotificationTopicEntity> notificationTopicsEntities = employeeUserEntity.getTopics();
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

            if (employeeUserEntity != null
                    && userTokensEntity.getNotificationToken().equals(subscriptionRequestDto.getToken())) {
                NotificationTopicEntity notificationTopicsEntity = notificationTopicsRepository
                        .getByTopic(subscriptionRequestDto.getTopicName());
                Set<NotificationTopicEntity> notificationTopicsEntities = employeeUserEntity.getTopics();
                if (notificationTopicsEntities.remove(notificationTopicsEntity)) {
                    FirebaseMessaging.getInstance(firebaseApp).unsubscribeFromTopic(
                            Arrays.asList(subscriptionRequestDto.getToken()),
                            subscriptionRequestDto.getTopicName());
                    employeeUserEntity.setTopics(notificationTopicsEntities);
                    employeeUserRepository.save(employeeUserEntity);
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
        Set<NotificationTopicEntity> userTopics = employeeUserEntity.getTopics();
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
                for(NotificationTopicEntity topic : userTopics){
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
        Iterable<ShopsEntity> shopsEntities = shopsRepository.findAll();
        List<NotificationTopicEntity> notificationTopicsEntities = new ArrayList<>();

        for(OrganizationEntity org : organizationEntities){
            getOrCreateTopicFor(org);
        }

        for(ShopsEntity shop : shopsEntities){
            getOrCreateTopicFor(shop);
        }
        notificationTopicsRepository.saveAll(notificationTopicsEntities);
    }

    private NotificationTopicEntity getOrCreateTopicFor(OrganizationEntity organization) {
        NotificationTopicEntity topic = Optional.ofNullable(organization.getNotificationTopic())
                .orElseGet(() -> createTopic(TopicType.ORG.getValue() + organization.getName()));
        organization.setNotificationTopic(topic);
        organizationRepository.save(organization);
        return topic;
    }

    private NotificationTopicEntity getOrCreateTopicFor(ShopsEntity shop) {
        NotificationTopicEntity topic = Optional.ofNullable(shop.getNotificationTopic())
                .orElseGet(() -> createTopic(TopicType.SHOP.getValue() + shop.getName()));
        shop.setNotificationTopic(topic);
        shopsRepository.save(shop);
        return topic;
    }


    private NotificationTopicEntity createTopic(String topicName) {
        NotificationTopicEntity notificationTopicsEntity = new NotificationTopicEntity(topicName);
        return notificationTopicsRepository.save(notificationTopicsEntity);
    }

    @Override
    public NotificationRequestDto getTopicByTopicName(String topicName) {
        NotificationTopicEntity notificationTopicsEntity = notificationTopicsRepository.getByTopic(topicName);
        if(notificationTopicsEntity == null){
            return null;
        }
        else {
            return new NotificationRequestDto(notificationTopicsEntity.getTopic());
        }
    }

    @Override
    public boolean logoutNotificationTokenCleaner(String authToken) {
        UserTokensEntity userTokensEntity = userTokenRepository.getUserEntityByToken(authToken);
        EmployeeUserEntity employeeUserEntity = userTokensEntity.getEmployeeUserEntity();
        if(employeeUserEntity != null){
            Set<NotificationTopicEntity> topicsEntities = employeeUserEntity.getTopics();
            try {
                for(NotificationTopicEntity topic : topicsEntities){
                    FirebaseMessaging.getInstance(firebaseApp).unsubscribeFromTopic(
                            Arrays.asList(userTokensEntity.getNotificationToken()),
                            topic.getTopic());
                }
            }
            catch (FirebaseMessagingException e){
                log.error("can't unsubscribeFromTopic in firebase", e);
                return false;
            }
        }
        return true;
    }

    private void loginFirstTimeNotificationToken(EmployeeUserEntity employeeUser, String token) {
        List<NotificationTopicEntity> topics = Collections.emptyList();

        Long orgId = employeeUser.getOrganizationId();
        organizationRepository.findById(orgId).map(this::getOrCreateTopicFor).ifPresent(topics::add);

        Long shopId = employeeUser.getShopId();
        shopsRepository.findById(shopId).map(this::getOrCreateTopicFor).ifPresent(topics::add);

        try {
            for (NotificationTopicEntity topic : topics) {
                FirebaseMessaging.getInstance(firebaseApp).subscribeToTopic(Arrays.asList(token),
                        topic.getTopic());

                NotificationTopicEntity notificationTopicsEntity = notificationTopicsRepository
                        .getByTopic(topic.getTopic());
                Set<NotificationTopicEntity> notificationTopicsEntities = employeeUser.getTopics();
                if (notificationTopicsEntities.add(notificationTopicsEntity)) {
                    employeeUser.setTopics(notificationTopicsEntities);
                    employeeUserRepository.save(employeeUser);
                }
            }
        } catch (FirebaseMessagingException e) {
            log.error("Can't subscribe to topic: " + e);
        }
    }

}
