package com.nasnav.service.notification;

import com.nasnav.dto.request.notification.NotificationRequestDto;
import com.nasnav.dto.request.notification.SubscriptionRequestDto;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.NotificationTopicEntity;

public interface NotificationService {
    /**
     * consumed from service
     * this method for subcribe Employee user to topic
     * it takes the emplyee user and topic then update the relation between employee entity
     * @param employee
     * @param topic
     */
    public void subscribeToTopic(EmployeeUserEntity employee, NotificationTopicEntity topic);

    /**
     * the oppsite of subscribe method that it deletes the relation with
     * the topic Entity and unsubscribe from fire base
     * @param employee
     * @param topic
     */
    public void unsubscribeFromTopic(EmployeeUserEntity employee, NotificationTopicEntity topic);

    /**
     * (un)subscribes to (un)needed notification topics
     */
    public void updateEmployeeTopics(EmployeeUserEntity employee);

    /**
     * send notification to specific token
     * @param notificationRequestDto
     */
    public void sendPnsToDevice(NotificationRequestDto notificationRequestDto);

    /**
     * send to group of tokens ---> a topic
     * @param notificationRequestDto
     */
    public void sendPnsToTopic(NotificationRequestDto notificationRequestDto);

    /**
     * update employee token be this it should update the token relation to topics in fire base
     * @param token
     * @return
     */
    public boolean createOrUpdateEmployeeToken(String token, String authToken);

    /**
     * update or create normal user token
     * @param token
     * @return
     */
    public boolean createOrUpdateCustomerToken(String token, String authToken);

    /**
     * this method refresh all topics of org and shop
     */
    public void refreshNotificationTopics();

    /**
     * getting the notification dto request by Topic Name
     * @return
     */
    public NotificationRequestDto getTopicByTopicName(String topicName);

    /**
     * this method unsubscribe from all topics WRT token
     * @param notificationToken
     * @param authToken
     * @return
     */
    public boolean logoutNotificationTokenCleaner(String authToken);


}
