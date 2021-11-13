package com.nasnav.service;

import com.nasnav.dto.GiftDTO;
import com.nasnav.persistence.LoyaltyGiftEntity;
import com.nasnav.response.LoyaltyGiftUpdateResponse;

import java.util.List;

public interface LoyaltyGiftService {

    LoyaltyGiftUpdateResponse sendGiftFromUserToAnother(GiftDTO dto);
    List<LoyaltyGiftEntity> getGiftsByUserId(Long userId);
    List<LoyaltyGiftEntity> getGiftsNotRedeemByUserId(Long userId);
    List<LoyaltyGiftEntity> getGiftsRedeemByUserReceiveId(Long userId);
    void updateOrCreateLoyaltyGiftTransaction(Long giftId);
}
