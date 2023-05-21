package com.nasnav.service.notification;

import com.nasnav.dto.request.notification.NotificationRequestDto;
import com.nasnav.persistence.EmployeeUserEntity;

public interface NotificationService {


    /**
     * send notification to specific token
     * @param notificationRequestDto
     */
    public void sendPnsToDevice(NotificationRequestDto notificationRequestDto);


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




}
