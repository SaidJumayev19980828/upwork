package com.nasnav.service;

import com.nasnav.persistence.CoinsDropEntity;
import com.nasnav.persistence.CoinsDropLogsEntity;

import java.util.Optional;

public interface CoinsDropLogsService {

    Optional<CoinsDropLogsEntity> getByOrganizationId(Long orgId);

    CoinsDropLogsEntity getByOrganizationIdAndCoinsDropId(Long orgId, Long coinsDropId);

    CoinsDropLogsEntity getByOrganizationIdAndUserId(Long orgId, Long userId);

    CoinsDropLogsEntity getByOrganizationIdAndCoinsDropIdAndUserId(Long orgId, Long coinsDropId, Long userId);

    Long updateCoinsDropLog(CoinsDropEntity coins);

}
