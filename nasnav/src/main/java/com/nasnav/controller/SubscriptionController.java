package com.nasnav.controller;


import com.nasnav.dto.SubscriptionDTO;
import com.nasnav.dto.SubscriptionInfoDTO;
import com.nasnav.service.impl.subscription.WertSubscriptionServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@AllArgsConstructor
@CrossOrigin("*")
@RequestMapping("/subscription")
public class SubscriptionController {


    private final WertSubscriptionServiceImpl wertSubscriptionService;

    @GetMapping
    @RequestMapping(value = "info", produces = APPLICATION_JSON_VALUE)
    public SubscriptionInfoDTO subscriptionInfo(@RequestHeader(name = "User-Token", required = false) String userToken){
        return wertSubscriptionService.getSubscriptionInfo();
    }

    @PostMapping
    @RequestMapping(value = "wert/createSubscription", produces = APPLICATION_JSON_VALUE)
    public SubscriptionDTO wertCreateSubscription(@RequestHeader(name = "User-Token", required = false) String userToken){
       return wertSubscriptionService.subscribe(new SubscriptionDTO());
    }
    }
}
