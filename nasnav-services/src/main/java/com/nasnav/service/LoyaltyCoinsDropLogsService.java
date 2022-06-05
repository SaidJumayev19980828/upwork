package com.nasnav.service;

import com.nasnav.persistence.LoyaltyCoinsDropEntity;
import com.nasnav.persistence.LoyaltyCoinsDropLogsEntity;

import java.util.Optional;

public interface LoyaltyCoinsDropLogsService {

    Optional<LoyaltyCoinsDropLogsEntity> getByOrganizationId(Long orgId);

    LoyaltyCoinsDropLogsEntity getByOrganizationIdAndCoinsDropId(Long orgId, Long coinsDropId);

    LoyaltyCoinsDropLogsEntity getByOrganizationIdAndUserId(Long orgId, Long userId);

    LoyaltyCoinsDropLogsEntity getByOrganizationIdAndCoinsDropIdAndUserId(Long orgId, Long coinsDropId, Long userId);

    Long updateCoinsDropLog(LoyaltyCoinsDropEntity coins);

}
