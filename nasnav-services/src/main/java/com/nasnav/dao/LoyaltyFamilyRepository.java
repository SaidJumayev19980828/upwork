package com.nasnav.dao;

import com.nasnav.persistence.LoyaltyFamilyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface LoyaltyFamilyRepository extends JpaRepository<LoyaltyFamilyEntity, Long> {

    List<LoyaltyFamilyEntity> getByOrganization_Id(Long orgId);

    @Transactional
    @Modifying
    void deleteByFamilyName(String familyName);

    Optional<LoyaltyFamilyEntity> findByFamilyName(String familyName);
}
