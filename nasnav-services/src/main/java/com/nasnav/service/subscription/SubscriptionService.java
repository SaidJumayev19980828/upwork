package com.nasnav.service.subscription;

import com.nasnav.dto.SubscriptionDTO;
import com.nasnav.dto.SubscriptionInfoDTO;
import com.nasnav.exceptions.RuntimeBusinessException;

import java.util.List;

public interface SubscriptionService {
    SubscriptionInfoDTO getSubscriptionInfo() throws RuntimeBusinessException;

    SubscriptionDTO getPaymentInfo(SubscriptionDTO subscriptionDTO) throws RuntimeBusinessException;

    SubscriptionDTO subscribe(SubscriptionDTO subscriptionDTO) throws RuntimeBusinessException;

    List<SubscriptionInfoDTO> getSubscriptionsByPackage(Long packageId);
}
