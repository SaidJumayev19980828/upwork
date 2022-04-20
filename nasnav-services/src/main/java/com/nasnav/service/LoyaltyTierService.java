package com.nasnav.service;

import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.LoyaltyTierDTO;
import com.nasnav.persistence.LoyaltyTierEntity;
import com.nasnav.response.LoyaltyTierUpdateResponse;

import java.util.List;
import java.util.Optional;

public interface LoyaltyTierService {

    void deleteTier(Long id);
    Optional<LoyaltyTierEntity> getTierById(Long id);
    List<LoyaltyTierEntity> getTiersBetweenAmountFromTo(Integer amountFrom, Integer amountTo);
    LoyaltyTierUpdateResponse updateTier(LoyaltyTierDTO tiers);
    LoyaltyTierEntity getTierByAmount(Integer amount);
    UserRepresentationObject changeUserTier(Long userId, Long tierId);
    List<LoyaltyTierDTO> getTiers(Long orgId, Boolean isSpecial);

}
