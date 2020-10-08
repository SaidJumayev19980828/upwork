package com.nasnav.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.OrganizationDomainsEntity;

public interface OrganizationDomainsRepository extends JpaRepository<OrganizationDomainsEntity, Long> {

    OrganizationDomainsEntity findByDomain(String domain);
    OrganizationDomainsEntity findByDomainAndSubdir(String domain, String subdir);
    List<OrganizationDomainsEntity> findByOrganizationEntity_IdOrderByIdDesc(Long orgId);
}
