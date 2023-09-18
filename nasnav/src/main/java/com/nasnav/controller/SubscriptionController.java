package com.nasnav.controller;


import com.nasnav.dto.SubscriptionDTO;
import com.nasnav.service.subscription.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subscription")
public class SubscriptionController {

    @Autowired
    SubscriptionService subscriptionService;

    @PostMapping
    @RequestMapping(value = "completeSubscription")
    public Long completeSubscription(@RequestBody SubscriptionDTO subscriptionDTO){
        return subscriptionService.completeSubscription(subscriptionDTO);
    }
}
