package com.nasnav.service.notification;

import com.nasnav.dto.request.notification.NotificationRequestDto;
import com.nasnav.dto.request.notification.SubscriptionRequestDto;
import com.nasnav.persistence.EmployeeUserEntity;

public interface NotificationService {
    /**
     * this method for subcribe Employee user to topic
     * it take the token and topic then update the relation between employee entity
     * also check if the token is need to be updated or not and update it
     * @param subscriptionRequestDto
     */
    public void subscribeToTopic(SubscriptionRequestDto subscriptionRequestDto);

    /**
     * the oppsite of subscribe method that it check first if the token is the same the delete the relation with
     * the topic Entity and then unsubscribe from fire base
     * @param subscriptionRequestDto
     */
    public void unsubscribeFromTopic(SubscriptionRequestDto subscriptionRequestDto);

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
    public boolean createOrUpdateEmployeeToken(String token);

    /**
     * update or create normal user token
     * @param token
     * @return
     */
    public boolean createOrUpdateUserToken(String token);

}
