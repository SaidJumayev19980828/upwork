package com.nasnav.controller;


import com.nasnav.dto.StripeSubscriptionDTO;
import com.nasnav.dto.SubscriptionDTO;
import com.nasnav.dto.SubscriptionInfoDTO;
import com.nasnav.dto.stripe.StripeSubscriptionPendingDTO;
import com.nasnav.service.StripeService;
import com.nasnav.service.impl.subscription.StripeSubscriptionServiceImpl;
import com.nasnav.service.impl.subscription.WertSubscriptionServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.stripe.model.Event;


import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@AllArgsConstructor
@CrossOrigin("*")
@RequestMapping("/subscription")
public class SubscriptionController {


    private final WertSubscriptionServiceImpl wertSubscriptionService;
    private final StripeSubscriptionServiceImpl stripeSubscriptionService;
    private final StripeService stripeService;

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


    //Stripe
    //1. Create
    //2. Change PaymentMethod
    //3. Change Package

    @PostMapping
    @RequestMapping(value = "stripe/createSubscription", produces = APPLICATION_JSON_VALUE)
    public StripeSubscriptionPendingDTO stripeCreateSubscription(@RequestHeader(name = "User-Token", required = false) String userToken){
        return ((StripeSubscriptionDTO) stripeSubscriptionService.subscribe(new SubscriptionDTO())).getStripeSubscriptionPendingDTO();
    }


    @PostMapping(value = "/stripe/webhook")
    public void stripeWebhook(@RequestHeader("Stripe-Signature") String signature, @RequestBody String body) {
        Event event = stripeService.verifyAndGetEventWebhook(signature,body);
        if ("customer.subscription.created".equals(event.getType())) {
            stripeSubscriptionService.handleStripeSubscriptionCreated(event);
        }
        else if ("customer.subscription.updated".equals(event.getType())) {
            stripeSubscriptionService.handleStripeSubscriptionUpdated(event);
        }
        else if ("customer.subscription.deleted".equals(event.getType())) {
            stripeSubscriptionService.handleStripeSubscriptionDeleted(event);
        }

    }



}
