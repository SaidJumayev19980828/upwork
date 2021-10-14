package com.nasnav.service;

import com.nasnav.dto.request.CoinsDropDTO;
import com.nasnav.persistence.CoinsDropEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.CoinUpdateResponse;

import java.util.List;

public interface CoinsDropService {

    CoinUpdateResponse updateCoinsDrop(CoinsDropDTO coins);

    List<CoinsDropEntity> getByOrganizationId(Long orgId);

    CoinsDropEntity getByOrganizationIdAndTypeId(Long orgId, Long typeId);

    void giveUsersCoinsBirthDay();

    void giveUsersCoinsOfficialFestival();

    void giveUsersCoinsOfficialRamadan();

    void giveUserCoinsNewTier(UserEntity user);

    void giveUserCoinsInvitationUsers(UserEntity user);

    void giveUserCoinsSignUp(UserEntity user);

    void giveUserCoinsNewFamilyMember(UserEntity user);

    void giveUserCoinsNewFamilyPurchase(UserEntity user);

}
