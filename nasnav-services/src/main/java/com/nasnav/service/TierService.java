package com.nasnav.service;

import com.nasnav.dto.request.TierDTO;
import com.nasnav.persistence.TierEntity;
import com.nasnav.response.TierUpdateResponse;

import java.util.List;
import java.util.Optional;

public interface TierService {

    void deleteTier(Long id);
    List<TierEntity> listTier();
    Optional<TierEntity> getTierById(Long id);
    List<TierEntity> getTiersBetweenAmountFromTo(Integer amountFrom, Integer amountTo);
    List<TierEntity> getTierByOrganization_Id(Long orgId);
    List<TierEntity> getTierByOrganization_IdAndIsSpecial(Long orgId, Boolean isSpecial);
    TierUpdateResponse updateTier(TierDTO tiers);
    TierEntity getTierByAmount(Integer amount);
    void addNewTierToUser(Long userId, Long tierId);

}
