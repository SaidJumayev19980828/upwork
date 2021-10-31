package com.nasnav.service;

import com.nasnav.dto.GiftDTO;
import com.nasnav.persistence.GiftEntity;
import com.nasnav.response.GiftUpdateResponse;

import java.util.List;

public interface GiftService {

    GiftUpdateResponse sendGiftFromUserToAnother(GiftDTO dto);
    List<GiftEntity> getGiftsByUserId(Long userId);
    List<GiftEntity> getGiftsNotRedeemByUserId(Long userId);
    List<GiftEntity> getGiftsRedeemByUserReceiveId(Long userId);
    void updateOrCreateLoyaltyGiftTransaction(Long giftId);
}
