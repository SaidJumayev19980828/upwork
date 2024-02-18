package com.nasnav.dao;

import com.nasnav.persistence.LoyaltyTierEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface LoyaltyTierRepository extends JpaRepository<LoyaltyTierEntity, Long> {
    List<LoyaltyTierEntity> getByOrganization_Id(Long orgId);

    List<LoyaltyTierEntity> getByOrganization_IdAndIsSpecial(Long orgId, Boolean isSpecial);

    @Query("select tier from LoyaltyTierEntity tier" +
            " where tier.noOfPurchaseFrom <= :amount AND tier.noOfPurchaseTo >= :amount" +
            " AND tier.isActive = true AND tier.organization.id = :organizationId")
    LoyaltyTierEntity getActiveByAmountInRangeAndOrganization_id(Integer amount, Long organizationId);

    @Query(value = """
                SELECT
                                 id, tier_name, is_active, is_special, created_at,
                                 no_of_purchase_from, no_of_purchase_to, selling_price,
                                 organization_id, booster_id, cash_back_percentage, constraints
                                 FROM
                                 public.loyalty_tier
                                 where is_active = true AND organization_id = :organizationId
                                 ORDER BY
                                 no_of_purchase_from
                                 LIMIT
                                 1
            """, nativeQuery = true )
    LoyaltyTierEntity getActiveDefaultTierAndOrganization_id( Long organizationId);


    Optional<LoyaltyTierEntity> findByTierName(String tierName);

    @Transactional
    @Modifying
    void deleteByTierName(String tierName);

    Optional<LoyaltyTierEntity> findByIdAndOrganization_Id(Long id, Long orgId);
}
