package com.nasnav.service;

import com.nasnav.dto.request.CharityDTO;
import com.nasnav.dto.request.UserCharityDTO;
import com.nasnav.response.CharityUpdateResponse;

public interface CharityService {

    CharityUpdateResponse updateCharity(CharityDTO dto);
    CharityUpdateResponse updateUserCharity(UserCharityDTO dto);
    void updateOrCreateLoyaltyUserCharityTransaction( Long charityId, Long userId, Long shopId);

}
