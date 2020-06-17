package com.nasnav.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.OrganizationShippingServiceEntity;

public interface OrganizationShippingServiceRepository extends JpaRepository<OrganizationShippingServiceEntity, Long> {
	List<OrganizationShippingServiceEntity> getByOrganization_Id(Long orgId);
}
