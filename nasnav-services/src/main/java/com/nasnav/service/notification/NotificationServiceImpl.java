package com.nasnav.service.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Sets;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.nasnav.AppConfig;
import com.nasnav.commons.utils.CollectionUtils;
import com.nasnav.dao.*;
import com.nasnav.dto.request.notification.NotificationRequestDto;
import com.nasnav.dto.request.notification.SubscriptionRequestDto;
import com.nasnav.enumerations.TopicType;
import com.nasnav.persistence.*;
import com.nasnav.service.SecurityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public void subscribeToTopic(EmployeeUserEntity employee, NotificationTopicEntity topic) {
        try {
            List<String> notificationTokens = securityService.getValidEmployeeNotificationTokens(employee).stream()
                    .filter(token -> !Objects.isNull(token)).collect(Collectors.toList());
            FirebaseMessaging.getInstance(firebaseApp).subscribeToTopic(notificationTokens,
                    topic.getTopic());
            Set<NotificationTopicEntity> notificationTopicsEntities = employee.getNotificationTopics();
            if(notificationTopicsEntities.add(topic)){
                employee.setNotificationTopics(notificationTopicsEntities);
                employeeUserRepository.save(employee);
            }
        } catch (FirebaseMessagingException e) {
            log.error("Firebase subscribe to topic fail", e);
        }
    }

    @Override
    public void unsubscribeFromTopic(EmployeeUserEntity employee, NotificationTopicEntity topic) {
        try {
            List<String> notificationTokens = securityService.getValidEmployeeNotificationTokens(employee).stream()
                    .filter(token -> !Objects.isNull(token)).collect(Collectors.toList());
            FirebaseMessaging.getInstance(firebaseApp).unsubscribeFromTopic(notificationTokens,
                    topic.getTopic());
            Set<NotificationTopicEntity> notificationTopicsEntities = employee.getNotificationTopics();
            if(notificationTopicsEntities.remove(topic)){
                employee.setNotificationTopics(notificationTopicsEntities);
                employeeUserRepository.save(employee);
            }
        } catch (FirebaseMessagingException e) {
            log.error("Firebase unsubscribe to topic fail", e);
        }
    }

    private void unsubscribeInvalidTokens(EmployeeUserEntity employee) {  
        List<String> notificationTokens = securityService.getInvalidEmployeeNotificationTokens(employee).stream()
                    .filter(token -> !Objects.isNull(token)).collect(Collectors.toList());
        Set<NotificationTopicEntity> topics = employee.getNotificationTopics();
        topics.stream().flatMap(NotificationTopicEntity::getEmployees).filter(Predicate.not(employee::equals))
        employee.getNotificationTopics().forEach(topic -> {
            try {
                FirebaseMessaging.getInstance(firebaseApp).unsubscribeFromTopic(notificationTokens,
                        topic.getTopic());
            } catch (FirebaseMessagingException e) {
                log.error("Firebase unsubscribe to topic fail", e);
            }
        });
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
        Set<NotificationTopicEntity> userTopics = employeeUserEntity.getNotificationTopics();
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
            Set<NotificationTopicEntity> topicsEntities = employeeUserEntity.getNotificationTopics();
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
                Set<NotificationTopicEntity> notificationTopicsEntities = employeeUser.getNotificationTopics();
                if (notificationTopicsEntities.add(notificationTopicsEntity)) {
                    employeeUser.setNotificationTopics(notificationTopicsEntities);
                    employeeUserRepository.save(employeeUser);
                }
            }
        } catch (FirebaseMessagingException e) {
            log.error("Can't subscribe to topic: " + e);
        }
    }

    @Override
    public void updateEmployeeTopics(EmployeeUserEntity employee) {
        Optional<NotificationTopicEntity> employeeOrgTopic = organizationRepository.findById(employee.getOrganizationId()).map(this::getOrCreateTopicFor);
        Optional<NotificationTopicEntity> employeeShopTopic = shopsRepository.findById(employee.getShopId()).map(this::getOrCreateTopicFor);

        Set<NotificationTopicEntity> expectedTopics = Stream.concat(employeeOrgTopic.stream(), employeeShopTopic.stream()).collect(Collectors.toSet());

        Set<NotificationTopicEntity> foundTopics = employee.getNotificationTopics();

        Set<NotificationTopicEntity> topicsToUnsubscribe = Sets.difference(foundTopics, expectedTopics);
        topicsToUnsubscribe.forEach(topic -> unsubscribeFromTopic(employee, topic));

        Set<NotificationTopicEntity> topicsToSubscribe = Sets.difference(expectedTopics, foundTopics);
        topicsToSubscribe.forEach(topic -> subscribeToTopic(employee, topic));
    }
}
