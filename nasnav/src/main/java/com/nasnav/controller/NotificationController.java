package com.nasnav.controller;

import com.nasnav.dto.request.notification.NotificationRequestDto;
import com.nasnav.dto.request.notification.SubscriptionRequestDto;
import com.nasnav.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/notification")
@CrossOrigin("*")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @PostMapping("/token")
    public void sendPnsToDevice(@RequestHeader(name = "User-Token") String userToken, @RequestBody NotificationRequestDto notificationRequestDto) {
        notificationService.sendPnsToDevice(notificationRequestDto);
    }

    @PostMapping("/topic")
    public void sendPnsToTopic(@RequestHeader(name = "User-Token") String userToken,@RequestBody NotificationRequestDto notificationRequestDto) {
        notificationService.sendPnsToTopic(notificationRequestDto);
    }

    @PostMapping("/subscribe")
    public void subscribeToTopic(@RequestHeader(name = "User-Token") String userToken,@RequestBody SubscriptionRequestDto subscriptionRequestDto) {
        notificationService.subscribeToTopic(subscriptionRequestDto);
    }

    @PostMapping("/unsubscribe")
    public void unsubscribeFromTopic(@RequestHeader(name = "User-Token") String userToken,@RequestBody SubscriptionRequestDto subscriptionRequestDto) {
        notificationService.unsubscribeFromTopic(subscriptionRequestDto);
    }

    @PostMapping("/updateToken")
    public void updateToken(@RequestHeader(name = "User-Token") String userToken,@RequestBody String newToken) {
        notificationService.createOrUpdateEmployeeToken(newToken,userToken);
    }

    @PostMapping("/user/updateToken")
    public void updateUserToken(@RequestHeader(name = "User-Token") String userToken,@RequestBody String newToken) {
        notificationService.createOrUpdateUserToken(newToken,userToken);
    }

    @PostMapping(value = "/refreshTopics")
    public void refreshTopics(@RequestHeader(name = "User-Token") String userToken){
        notificationService.refreshNotificationTopics();
    }
}
