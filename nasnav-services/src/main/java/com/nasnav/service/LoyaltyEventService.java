package com.nasnav.service;

import com.nasnav.dto.request.LoyaltyEventDTO;
import com.nasnav.response.LoyaltyEventUpdateResponse;

import java.util.List;

public interface LoyaltyEventService {
    LoyaltyEventUpdateResponse createUpdateEvent(LoyaltyEventDTO loyaltyEventDTO);

    void deleteById(Long id);

    List<LoyaltyEventDTO> getAllEvents(Long orgId);
}
