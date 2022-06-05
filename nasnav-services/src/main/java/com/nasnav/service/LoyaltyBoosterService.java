package com.nasnav.service;

import com.nasnav.dto.request.LoyaltyBoosterDTO;
import com.nasnav.response.LoyaltyBoosterUpdateResponse;

import java.util.List;

public interface LoyaltyBoosterService {

    LoyaltyBoosterUpdateResponse updateBooster(LoyaltyBoosterDTO dto);

    List<LoyaltyBoosterDTO> getBoosterByOrgId(Long orgId);

    List<LoyaltyBoosterDTO> getBoosters();

    LoyaltyBoosterDTO getBoosterById(Long id);

    void deleteBooster(Long boosterId);

    void upgradeUserBooster(Long boosterId, Long userId);
}
