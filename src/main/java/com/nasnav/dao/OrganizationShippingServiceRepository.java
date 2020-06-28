package com.nasnav.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.OrganizationShippingServiceEntity;

public interface OrganizationShippingServiceRepository extends JpaRepository<OrganizationShippingServiceEntity, Long> {
	List<OrganizationShippingServiceEntity> getByOrganization_Id(Long orgId);

	Optional<OrganizationShippingServiceEntity> getByOrganization_IdAndServiceId(Long orgId, String serviceId);
}
