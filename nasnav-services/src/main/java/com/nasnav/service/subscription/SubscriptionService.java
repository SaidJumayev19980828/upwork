package com.nasnav.service.subscription;

import com.nasnav.dto.SubscriptionDTO;
import com.nasnav.exceptions.RuntimeBusinessException;

public interface SubscriptionService {
    public Long completeSubscription(SubscriptionDTO subscriptionDTO) throws RuntimeBusinessException;

}
