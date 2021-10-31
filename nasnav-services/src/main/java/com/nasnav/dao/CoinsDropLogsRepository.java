package com.nasnav.dao;

import com.nasnav.persistence.CoinsDropLogsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoinsDropLogsRepository extends JpaRepository<CoinsDropLogsEntity, Long> {

    Optional<CoinsDropLogsEntity> getByOrganization_Id(Long orgId);

    CoinsDropLogsEntity getByOrganization_IdAndCoinsDrop_Id(Long orgId, Long coinsDropId);

    CoinsDropLogsEntity getByOrganization_IdAndCoinsDrop_IdAndUser_Id(Long orgId, Long coinsDropId, Long userId);

    CoinsDropLogsEntity getByOrganization_IdAndUser_Id(Long orgId, Long userId);
}
