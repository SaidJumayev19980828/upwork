package com.nasnav.service;

import com.nasnav.dto.request.LoyaltyCharityDTO;
import com.nasnav.dto.request.UserCharityDTO;
import com.nasnav.response.LoyaltyCharityUpdateResponse;

public interface LoyaltyCharityService {

    LoyaltyCharityUpdateResponse updateCharity(LoyaltyCharityDTO dto);
    LoyaltyCharityUpdateResponse updateUserCharity(UserCharityDTO dto);
    void updateOrCreateLoyaltyUserCharityTransaction( Long charityId, Long userId, Long shopId);

}
