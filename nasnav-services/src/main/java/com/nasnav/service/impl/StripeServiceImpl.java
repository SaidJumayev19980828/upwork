package com.nasnav.service.impl;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.PackageRepository;
import com.nasnav.dto.stripe.StripeSubscriptionPendingDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.PackageEntity;
import com.nasnav.service.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.*;

@Service
public class StripeServiceImpl implements StripeService {

    @Value("${stripe.apikey}")
    public String apiKey;

    @Value("${stripe.webhook.secret}")
    public String webhookSecret;

    @Autowired
    PackageRepository packageRepository;

    private static final Logger stripeLogger = LogManager.getLogger("Subscription:STRIPE");

    @PostConstruct
    public void init() {
        if(StringUtils.isBlankOrNull(apiKey)){
            stripeLogger.error("Fail To Load Api key of Stripe");
        }else{
            Stripe.apiKey = apiKey;
        }
    }

    public String createCustomer(String name ,String email){
        String customerId = null;
        try {
            CustomerCreateParams params =
                    CustomerCreateParams.builder()
                            .setEmail(email)
                            .setName(name)
                            .build();
            Customer customer = Customer.create(params);
            customerId = customer.getId();
        } catch (StripeException e) {
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, STR$CAL$0003);
        }

        return customerId;
    }



}