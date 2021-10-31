package com.nasnav.service;

import com.nasnav.dto.request.TierDTO;
import com.nasnav.persistence.TierEntity;
import com.nasnav.response.TierUpdateResponse;

import java.util.List;
import java.util.Optional;

public interface TierService {

    void deleteTier(Long id);
    Optional<TierEntity> getTierById(Long id);
    List<TierEntity> getTiersBetweenAmountFromTo(Integer amountFrom, Integer amountTo);
    TierUpdateResponse updateTier(TierDTO tiers);
    TierEntity getTierByAmount(Integer amount);
    void addNewTierToUser(Long userId, Long tierId);
    List<TierDTO> getTiers(Long orgId, Boolean isSpecial);
}
