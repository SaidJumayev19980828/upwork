package com.nasnav.service.subscription;

import com.nasnav.dto.SubscriptionDTO;
import com.nasnav.dto.SubscriptionInfoDTO;
import com.nasnav.exceptions.RuntimeBusinessException;

public interface SubscriptionService {
    public SubscriptionInfoDTO getSubscriptionInfo() throws RuntimeBusinessException;

    public SubscriptionDTO getPaymentInfo(SubscriptionDTO subscriptionDTO) throws RuntimeBusinessException;

    public SubscriptionDTO subscribe(SubscriptionDTO subscriptionDTO) throws RuntimeBusinessException;

}
