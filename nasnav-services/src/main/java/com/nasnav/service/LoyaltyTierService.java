package com.nasnav.service;

import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.LoyaltyTierDTO;
import com.nasnav.enumerations.LoyaltyPointType;
import com.nasnav.persistence.LoyaltyTierEntity;
import com.nasnav.response.LoyaltyTierUpdateResponse;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public interface LoyaltyTierService {

    void deleteTier(Long id);
    LoyaltyTierDTO getTierById(Long id);
    List<LoyaltyTierEntity> getTiersBetweenAmountFromTo(Integer amountFrom, Integer amountTo);
    LoyaltyTierUpdateResponse updateTier(LoyaltyTierDTO tiers);
    LoyaltyTierEntity getTierByAmount(Integer amount);
    LoyaltyTierEntity getTierByAmountAndOrganizationId(Integer integer, Long organizationId);
    UserRepresentationObject changeUserTier(Long userId, Long tierId);
    List<LoyaltyTierDTO> getTiers(Boolean isSpecial);

    HashMap<LoyaltyPointType, BigDecimal> readTierJsonStr(String jsonStr);
}
