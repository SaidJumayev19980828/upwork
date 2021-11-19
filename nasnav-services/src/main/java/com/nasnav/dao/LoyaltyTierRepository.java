package com.nasnav.dao;

import com.nasnav.persistence.LoyaltyTierEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface LoyaltyTierRepository extends JpaRepository<LoyaltyTierEntity, Long> {

    @Query("select tier from LoyaltyTierEntity tier" +
            " where tier.noOfPurchaseFrom >= :amountFrom AND tier.noOfPurchaseTo <= :amountTo" +
            " order by tier.id asc")
    List<LoyaltyTierEntity> getByAmountFromAndTo(@Param("amountFrom") Integer amountFrom, @Param("amountTo") Integer amountTo);

    List<LoyaltyTierEntity> getByOrganization_Id(Long orgId);

    List<LoyaltyTierEntity> getByOrganization_IdAndIsSpecial(Long orgId, Boolean isSpecial);

    @Query("select tier from LoyaltyTierEntity tier" +
            " where tier.noOfPurchaseFrom >= :amount AND tier.noOfPurchaseTo <= :amount" +
            " order by tier.id asc")
    LoyaltyTierEntity getByAmount(@Param("amount") Integer amount);

    Optional<LoyaltyTierEntity> findByTierName(String tierName);

    @Transactional
    @Modifying
    void deleteByTierName(String tierName);
}
