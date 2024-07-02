package com.nasnav.dao;

import com.nasnav.persistence.OrganizationServicesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationServicesRepository extends JpaRepository<OrganizationServicesEntity, Long> {

    List<OrganizationServicesEntity> findAllByOrgId(Long orgId);

    List<OrganizationServicesEntity> findAllByServiceId(Long serviceId);

    OrganizationServicesEntity getByOrgIdAndServiceId(Long orgId, Long serviceId);

    List<OrganizationServicesEntity> findAllByOrgIdAndServiceId(Long orgId, Long serviceId);
}
