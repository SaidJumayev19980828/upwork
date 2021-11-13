package com.nasnav.service;

import com.nasnav.dto.request.LoyaltyCoinsDropDTO;
import com.nasnav.persistence.LoyaltyCoinsDropEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.LoyaltyCoinUpdateResponse;

import java.util.List;

public interface LoyaltyCoinsDropService {

    LoyaltyCoinUpdateResponse updateCoinsDrop(LoyaltyCoinsDropDTO coins);

    List<LoyaltyCoinsDropEntity> getByOrganizationId(Long orgId);

    LoyaltyCoinsDropEntity getByOrganizationIdAndTypeId(Long orgId, Integer typeId);

    void giveUsersCoinsBirthDay();

    void giveUsersCoinsOfficialFestival();

    void giveUsersCoinsOfficialRamadan();

    void giveUserCoinsNewTier(UserEntity user);

    void giveUserCoinsInvitationUsers(UserEntity user);

    void giveUserCoinsSignUp(UserEntity user);

    void giveUserCoinsNewFamilyMember(UserEntity user);

    void giveUserCoinsNewFamilyPurchase(UserEntity user);

}
