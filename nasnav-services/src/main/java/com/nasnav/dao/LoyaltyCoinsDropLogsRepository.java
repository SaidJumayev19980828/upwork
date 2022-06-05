package com.nasnav.dao;

import com.nasnav.persistence.LoyaltyCoinsDropLogsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoyaltyCoinsDropLogsRepository extends JpaRepository<LoyaltyCoinsDropLogsEntity, Long> {

    Optional<LoyaltyCoinsDropLogsEntity> getByOrganization_Id(Long orgId);

    LoyaltyCoinsDropLogsEntity getByOrganization_IdAndCoinsDrop_Id(Long orgId, Long coinsDropId);

    LoyaltyCoinsDropLogsEntity getByOrganization_IdAndCoinsDrop_IdAndUser_Id(Long orgId, Long coinsDropId, Long userId);

    LoyaltyCoinsDropLogsEntity getByOrganization_IdAndUser_Id(Long orgId, Long userId);
}
