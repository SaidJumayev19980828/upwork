package com.nasnav.dao;

import com.nasnav.persistence.OrganizationDomainsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationDomainsRepository extends JpaRepository<OrganizationDomainsEntity, Long> {

    OrganizationDomainsEntity findByDomain(String domain);
    OrganizationDomainsEntity findByDomainAndSubdir(String domain, String subdir);
    OrganizationDomainsEntity findByOrganizationEntity_Id(Long orgId);
}
