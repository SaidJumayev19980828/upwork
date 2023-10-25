package com.nasnav.service;

import com.nasnav.dto.stripe.StripeConfirmDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.PackageEntity;
import com.stripe.model.Event;
import com.stripe.model.SetupIntent;
import com.stripe.model.Subscription;

public interface StripeWebhookSubscriptionService {


    void handleStripeSubscriptionCreated(Event event) throws RuntimeBusinessException;

    void handleStripeSubscriptionUpdated(Event event) throws RuntimeBusinessException;

    void handleStripeSubscriptionDeleted(Event event) throws RuntimeBusinessException;

    void handleStripeSetupIntent(Event event) throws RuntimeBusinessException;

}
