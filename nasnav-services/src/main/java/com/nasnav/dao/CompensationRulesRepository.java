package com.nasnav.dao;

import com.nasnav.persistence.CompensationRulesEntity;
import com.nasnav.persistence.OrganizationEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CompensationRulesRepository extends JpaRepository<CompensationRulesEntity, Long> {

    Optional<CompensationRulesEntity> findByIdAndOrganization(Long ruleId,OrganizationEntity organizationEntity);


    List<CompensationRulesEntity> findAllByOrganizationAndIsActiveTrue(OrganizationEntity organizationEntity);

    Optional<CompensationRulesEntity> findByIdAndOrganizationAndIsActiveTrue(Long ruleId,OrganizationEntity organizationEntity);

    PageImpl<CompensationRulesEntity> findAllByOrganization(OrganizationEntity organizationEntity, Pageable pageable);


    @Query("""
            Select case when count(r) > 0 then true else false end
            from AdvertisementEntity a
            join  a.advertisementProducts adp
            join  adp.compensationRules crs
            join  crs.compensationRule r
            where r = :rule AND a.toDate >= current_date
            and r.isActive = true
            """)
    boolean checkIfRuleInUseNow(CompensationRulesEntity rule);
}