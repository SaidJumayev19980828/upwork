package com.nasnav.dao;

import com.nasnav.persistence.TierEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface TierRepository extends JpaRepository<TierEntity, Long> {

    @Query("select tier from TierEntity tier" +
            " where tier.noOfPurchaseFrom >= :amountFrom AND tier.noOfPurchaseTo <= :amountTo" +
            " order by tier.id asc")
    List<TierEntity> getByAmountFromAndTo(@Param("amountFrom") Integer amountFrom,@Param("amountTo") Integer amountTo);

    List<TierEntity> getByOrganization_Id(Long orgId);

    List<TierEntity> getByOrganization_IdAndIsSpecial(Long orgId, Boolean isSpecial);

    @Query("select tier from TierEntity tier" +
            " where tier.noOfPurchaseFrom >= :amount AND tier.noOfPurchaseTo <= :amount" +
            " order by tier.id asc")
    TierEntity getByAmount(@Param("amount") Integer amount);

    Optional<TierEntity> findByTierName(String tierName);

    @Transactional
    @Modifying
    void deleteByTierName(String tierName);
}
