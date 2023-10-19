package com.nasnav.service.subscription;

import com.nasnav.dto.SubscriptionDTO;
import com.nasnav.dto.SubscriptionInfoDTO;
import com.nasnav.dto.stripe.StripeConfirmDTO;
import com.nasnav.exceptions.RuntimeBusinessException;

public interface StripeSubscriptionService extends SubscriptionService {

    boolean checkOrgHasStripeCustomer();

    /**
     * Used to create customer if not exist
     * @return the id of the customer
     */
    String getOrCreateStripeCustomer();

    StripeConfirmDTO setupIntent() throws RuntimeBusinessException;

    void cancelSubscription() throws RuntimeBusinessException;

    void changePlan() throws RuntimeBusinessException;
}
