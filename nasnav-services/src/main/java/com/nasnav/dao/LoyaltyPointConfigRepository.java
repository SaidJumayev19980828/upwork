package com.nasnav.dao;

import com.nasnav.persistence.LoyaltyPointConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface LoyaltyPointConfigRepository extends JpaRepository<LoyaltyPointConfigEntity, Long> {

    @Query("SELECT loyaltyConfig FROM LoyaltyPointConfigEntity loyaltyConfig" +
            " WHERE loyaltyConfig.isActive = true AND loyaltyConfig.defaultTier.id = :id" +
            " AND loyaltyConfig.defaultTier.isActive = true"

    )
    Optional<LoyaltyPointConfigEntity> findActiveConfigTierByTierId(Long id);

    Optional<LoyaltyPointConfigEntity> findByIdAndOrganization_IdAndIsActive(Long id, Long orgId, Boolean isActive);


    Optional<LoyaltyPointConfigEntity> findByDefaultTier_IdAndIsActive(Long defaultTierId, Boolean isActive);

    List<LoyaltyPointConfigEntity> findByOrganization_IdOrderByCreatedAtDesc(Long orgId);

    Optional<LoyaltyPointConfigEntity> findByOrganization_IdAndIsActive(@Param("orgId") Long orgId, Boolean isActive);

    @Transactional
    @Modifying
    @Query("update LoyaltyPointConfigEntity c set c.isActive = false where c.organization.id = :orgId")
    void setAllOrgConfigsAsInactive(@Param("orgId") Long orgId);

    @Transactional
    @Modifying
    @Query("delete from LoyaltyPointConfigEntity c where c.defaultTier.id = :tierId and c.organization.id = :orgId")
    void deleteSoftDeletedConfigs(@Param("orgId") Long orgId,
                                  @Param("tierId") Long tierId);


    List<LoyaltyPointConfigEntity> findByIsActiveAndOrganizationYeshteryStateOrderByCreatedAtDesc(boolean isActive, int yeshteryState);
}
