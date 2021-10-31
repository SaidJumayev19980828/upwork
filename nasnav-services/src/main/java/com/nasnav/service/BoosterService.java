package com.nasnav.service;

import com.nasnav.dto.request.BoosterDTO;
import com.nasnav.response.LoyaltyBoosterUpdateResponse;

import java.util.List;

public interface BoosterService {

    LoyaltyBoosterUpdateResponse updateBooster(BoosterDTO dto);

    List<BoosterDTO> getBoosterByOrgId(Long orgId);

    List<BoosterDTO> getBoosters();

    BoosterDTO getBoosterById(Long id);

    void deleteBooster(Long boosterId);

    void upgradeUserBooster(Long boosterId, Long userId);
}
