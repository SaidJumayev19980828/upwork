package com.nasnav.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.OrganizationDomainsEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface OrganizationDomainsRepository extends JpaRepository<OrganizationDomainsEntity, Long> {

    Optional<OrganizationDomainsEntity> findByIdAndOrganizationEntity_Id(Long id, Long orgId);
    OrganizationDomainsEntity findByDomain(String domain);
    OrganizationDomainsEntity findByDomainAndSubdir(String domain, String subdir);
    boolean existsByDomainAndSubdir(String domain, String subdir);

    @Transactional
    @Modifying
    @Query("update OrganizationDomainsEntity domain set domain.priority = 0 where domain.organizationEntity.id = :orgId")
    void resetOrganizationDomainsCanonical(@Param("orgId") Long orgId);

    List<OrganizationDomainsEntity> findByOrganizationEntity_IdOrderByPriorityDescIdDesc(Long orgId);

    @Transactional
    @Modifying
    void deleteByIdAndOrganizationEntity_Id(Long id, Long orgId);
}
