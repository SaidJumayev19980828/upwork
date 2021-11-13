package com.nasnav.dao;

import com.nasnav.persistence.LoyaltyBoosterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyBoosterRepository extends JpaRepository<LoyaltyBoosterEntity, Long> {

    List<LoyaltyBoosterEntity> getAllByOrganization_IdAndIsActiveTrue(Long orgId);

    boolean existsByIdAndOrganization_Id(Long id, Long orgId);

    Optional<LoyaltyBoosterEntity> getByIdAndOrganization_Id(Long id, Long orgId);

    @Query("select b from LoyaltyBoosterEntity b where b.isActive = true and b.activationMonths <= :activationMonths")
    List<LoyaltyBoosterEntity> getAllByActivationMonths(@Param("activationMonths") Integer activationMonths);

    @Query("select b from LoyaltyBoosterEntity b where b.isActive = true and b.purchaseSize <= :purchaseSize")
    List<LoyaltyBoosterEntity> getAllByPurchaseSize(@Param("purchaseSize") Integer purchaseSize);

    @Query("select b from LoyaltyBoosterEntity b where b.isActive = true and b.reviewProducts <= :reviewProducts")
    List<LoyaltyBoosterEntity> getAllByReviewProducts(@Param("reviewProducts") Integer reviewProducts);

    @Query("select b from LoyaltyBoosterEntity b where b.isActive = true and b.linkedFamilyMember <= :familyMember")
    List<LoyaltyBoosterEntity> getAllByLinkedFamilyMember(@Param("familyMember") Integer familyMember);

    @Query("select b from LoyaltyBoosterEntity b where b.isActive = true and b.numberFamilyChildren <= :familyChildren")
    List<LoyaltyBoosterEntity> getAllByNumberFamilyChildren(@Param("familyChildren") Integer familyChildren);

    @Query("select b from LoyaltyBoosterEntity b where b.isActive = true and b.numberPurchaseOffline <= :purchaseOffline")
    List<LoyaltyBoosterEntity> getAllByNumberPurchaseOffline(@Param("purchaseOffline") Integer purchaseOffline);

    Optional<LoyaltyBoosterEntity> findByBoosterName(String name);


    @Transactional
    @Modifying
    void deleteByBoosterName(String s);
}
