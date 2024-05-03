package com.nasnav.dao;

import com.nasnav.persistence.CompensationRulesEntity;
import com.nasnav.persistence.OrganizationEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompensationRulesRepository extends JpaRepository<CompensationRulesEntity, Long> {

    Optional<CompensationRulesEntity> findByIdAndOrganization(Long ruleId,OrganizationEntity organizationEntity);


    List<CompensationRulesEntity> findAllByOrganizationAndIsActiveTrue(OrganizationEntity organizationEntity);

    Optional<CompensationRulesEntity> findByIdAndOrganizationAndIsActiveTrue(Long ruleId,OrganizationEntity organizationEntity);

    PageImpl<CompensationRulesEntity> findAllByOrganization(OrganizationEntity organizationEntity, Pageable pageable);
}