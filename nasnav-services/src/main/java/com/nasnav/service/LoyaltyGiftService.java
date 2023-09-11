package com.nasnav.service;

import com.nasnav.dto.GiftDTO;
import com.nasnav.persistence.LoyaltyGiftEntity;
import com.nasnav.response.LoyaltyGiftUpdateResponse;

import java.util.List;

public interface LoyaltyGiftService {

    LoyaltyGiftUpdateResponse sendGiftFromUserToAnother(GiftDTO dto);
    List<LoyaltyGiftEntity> getGiftsByUserId(Long userId);
    List<LoyaltyGiftEntity> getGiftsByUserIdAndIsRedeem(Long userId, boolean isRedeem);
    void updateOrCreateLoyaltyGiftTransaction(Long giftId);
}
