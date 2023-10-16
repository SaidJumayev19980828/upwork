package com.nasnav.service;

import com.nasnav.dto.stripe.StripeConfirmDTO;
import com.nasnav.persistence.PackageEntity;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;

public interface StripeService {

    String createCustomer(String name , String email);

    StripeConfirmDTO createSubscription(String stripePriceId , String customerId);

    StripeConfirmDTO setupIntent(String customerId);

    StripeObject getStripeObjectFromWebhookEvent(Event event);


    void updateCustomerDefaultPaymentMethod(String customerId, String paymentMethodId);

    void updateSubscriptionDefaultPaymentMethod(String subscriptionId, String paymentMethodId);

    Event verifyAndGetEventWebhook(String signatureHeader , String body);

    PackageEntity getPackageByStripeSubscription(Subscription subscription);
    void lastOpenInvoicePayRetry(String customerId , String subscriptionId);

    void cancelSubscription(String subscriptionId);
    Subscription changePlan(String subscriptionId, String priceId);

}
