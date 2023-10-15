package com.nasnav.service;

import com.nasnav.dto.stripe.StripeSubscriptionPendingDTO;
import com.nasnav.persistence.PackageEntity;
import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;

import java.math.BigDecimal;

public interface StripeService {

    String createCustomer(String name , String email);

    StripeSubscriptionPendingDTO createSubscription(String stripePriceId , String customerId);


    Subscription getSubscriptionFromWebhookEvent(Event event);

    Event verifyAndGetEventWebhook(String signatureHeader , String body);

    PackageEntity getPackageByStripeSubscription(Subscription subscription);
}
