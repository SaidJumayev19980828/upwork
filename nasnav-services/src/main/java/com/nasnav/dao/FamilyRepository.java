package com.nasnav.dao;

import com.nasnav.persistence.FamilyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface FamilyRepository extends JpaRepository<FamilyEntity, Long> {

    List<FamilyEntity> getByOrganization_Id(Long orgId);

    void deleteByFamilyName(String familyName);


    @Transactional
    @Modifying
    Optional<FamilyEntity> findByFamilyName(String familyName);
}
