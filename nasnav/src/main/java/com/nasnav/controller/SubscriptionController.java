package com.nasnav.controller;


import com.nasnav.dto.SubscriptionDTO;
import com.nasnav.dto.request.PackageRegisteredByUserDTO;
import com.nasnav.service.BankInsideTransactionService;
import com.nasnav.service.PackageService;
import com.nasnav.service.impl.subscription.WertSubscriptionServiceImpl;
import com.nasnav.service.subscription.SubscriptionService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@AllArgsConstructor
@CrossOrigin("*")
@RequestMapping("/subscription")
public class SubscriptionController {


    private final WertSubscriptionServiceImpl wertSubscriptionService;


    @PostMapping
    @RequestMapping(value = "wertSubscription", produces = APPLICATION_JSON_VALUE)
    public Long wertSubscription(@RequestHeader(name = "User-Token", required = false) String userToken,@RequestBody SubscriptionDTO subscriptionDTO){
       return wertSubscriptionService.subscribe(subscriptionDTO);
    }
}
