package com.nasnav.dao;

import com.nasnav.persistence.OrganizationServicesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationServicesRepository extends JpaRepository<OrganizationServicesEntity, Long> {

    List<OrganizationServicesEntity> findAllByOrgId(Long orgId);
    void deleteAllByOrgId(Long orgId);

}
