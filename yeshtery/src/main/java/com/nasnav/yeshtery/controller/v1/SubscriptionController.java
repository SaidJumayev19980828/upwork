package com.nasnav.yeshtery.controller.v1;


import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dto.StripeSubscriptionDTO;
import com.nasnav.dto.SubscriptionDTO;
import com.nasnav.dto.SubscriptionInfoDTO;
import com.nasnav.dto.stripe.StripeConfirmDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.service.StripeService;
import com.nasnav.service.StripeWebhookSubscriptionService;
import com.nasnav.service.subscription.StripeSubscriptionService;
import com.nasnav.service.subscription.SubscriptionService;
import com.stripe.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import static com.nasnav.exceptions.ErrorCodes.STR$CAL$0004;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@CrossOrigin("*")
@RequestMapping(value = SubscriptionController.API_PATH, produces = APPLICATION_JSON_VALUE)
public class SubscriptionController {

    static final String API_PATH = YeshteryConstants.API_PATH + "/subscription";

    @Autowired
    @Qualifier("wert")
    private SubscriptionService wertSubscriptionService;
    @Autowired
    @Qualifier("stripe")
    private StripeSubscriptionService stripeSubscriptionService;
    @Autowired
    private StripeWebhookSubscriptionService stripeWebhookSubscriptionService;
    @Autowired
    private StripeService stripeService;
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);


    @GetMapping
    @RequestMapping(value = "info", produces = APPLICATION_JSON_VALUE)
    public SubscriptionInfoDTO subscriptionInfo(@RequestHeader(name = "User-Token", required = false) String userToken){
        return wertSubscriptionService.getSubscriptionInfo();
    }

    @PostMapping
    @RequestMapping(value = "wert/create", produces = APPLICATION_JSON_VALUE)
    public SubscriptionDTO wertCreateSubscription(@RequestHeader(name = "User-Token", required = false) String userToken){
        return wertSubscriptionService.subscribe(new SubscriptionDTO());
    }


    //Stripe
    //1. Create
    //2. Change PaymentMethod
    //3. Change Package

    @PostMapping
    @RequestMapping(value = "stripe/create", produces = APPLICATION_JSON_VALUE)
    public StripeConfirmDTO stripeCreateSubscription(@RequestHeader(name = "User-Token", required = false) String userToken){
        logger.info("Stripe Create Subscription Starts");
        StripeSubscriptionDTO stripeSubscriptionDTO = ((StripeSubscriptionDTO) stripeSubscriptionService.subscribe(new SubscriptionDTO()));
        if(stripeSubscriptionDTO == null){
            logger.error("Failed To Subscribe In Stripe");
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, STR$CAL$0004);
        }
        return stripeSubscriptionDTO.getStripeConfirmDTO();
    }

    @PostMapping
    @RequestMapping(value = "stripe/changePaymentMethod", produces = APPLICATION_JSON_VALUE)
    public StripeConfirmDTO stripeSetupIntent(@RequestHeader(name = "User-Token", required = false) String userToken){
        return stripeSubscriptionService.setupIntent();
    }

    @PostMapping
    @RequestMapping(value = "stripe/cancel", produces = APPLICATION_JSON_VALUE)
    public void stripeCancelSubscription(@RequestHeader(name = "User-Token", required = false) String userToken){
        stripeSubscriptionService.cancelSubscription();
    }

    @PostMapping
    @RequestMapping(value = "stripe/changePlan", produces = APPLICATION_JSON_VALUE)
    public void stripeChangePlan(@RequestHeader(name = "User-Token", required = false) String userToken){
        stripeSubscriptionService.changePlan();
    }

    @PostMapping(value = "/stripe/webhook")
    public void stripeWebhook(@RequestHeader("Stripe-Signature") String signature, @RequestBody String body) {
        logger.info("stripe webhook request start");
        Event event = stripeService.verifyAndGetEventWebhook(signature,body);
        try {
            logger.info("stripe webhook: " + event.getType());
            if ("customer.subscription.created".equals(event.getType())) {
                stripeWebhookSubscriptionService.handleStripeSubscriptionCreated(event);
            }
            else if ("customer.subscription.updated".equals(event.getType())) {
                stripeWebhookSubscriptionService.handleStripeSubscriptionUpdated(event);
            }
            else if ("customer.subscription.deleted".equals(event.getType())) {
                stripeWebhookSubscriptionService.handleStripeSubscriptionDeleted(event);
            }
            else if ("setup_intent.succeeded".equals(event.getType())) {
                stripeWebhookSubscriptionService.handleStripeSetupIntent(event);
            }


        }catch (Exception ex){
            logger.error("Webhook Error : " + ex.getMessage());
        }
    }

}
