package com.nasnav.dao;

import com.nasnav.persistence.BoosterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoosterRepository extends JpaRepository<BoosterEntity, Long> {

    List<BoosterEntity> getAllByOrganization_IdAndIsActiveTrue(Long orgId);

    boolean existsByIdAndOrganization_Id(Long id, Long orgId);

    Optional<BoosterEntity> getByIdAndOrganization_Id(Long id, Long orgId);

    @Query("select b from BoosterEntity b where b.isActive = true and b.activationMonths <= :activationMonths")
    List<BoosterEntity> getAllByActivationMonths(@Param("activationMonths") Integer activationMonths);

    @Query("select b from BoosterEntity b where b.isActive = true and b.purchaseSize <= :purchaseSize")
    List<BoosterEntity> getAllByPurchaseSize(@Param("purchaseSize") Integer purchaseSize);

    @Query("select b from BoosterEntity b where b.isActive = true and b.reviewProducts <= :reviewProducts")
    List<BoosterEntity> getAllByReviewProducts(@Param("reviewProducts") Integer reviewProducts);

    @Query("select b from BoosterEntity b where b.isActive = true and b.linkedFamilyMember <= :familyMember")
    List<BoosterEntity> getAllByLinkedFamilyMember(@Param("familyMember") Integer familyMember);

    @Query("select b from BoosterEntity b where b.isActive = true and b.numberFamilyChildren <= :familyChildren")
    List<BoosterEntity> getAllByNumberFamilyChildren(@Param("familyChildren") Integer familyChildren);

    @Query("select b from BoosterEntity b where b.isActive = true and b.numberPurchaseOffline <= :purchaseOffline")
    List<BoosterEntity> getAllByNumberPurchaseOffline(@Param("purchaseOffline") Integer purchaseOffline);

    Optional<BoosterEntity> findByBoosterName(String name);


    @Transactional
    @Modifying
    void deleteByBoosterName(String s);
}
