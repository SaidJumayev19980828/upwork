package com.nasnav.controller;

import com.nasnav.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/notification")
@CrossOrigin("*")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @PostMapping(value = "/refreshTopics")
    public void refreshTopics(@RequestHeader(name = "User-Token") String userToken){
        notificationService.refreshNotificationTopics();
    }
}
