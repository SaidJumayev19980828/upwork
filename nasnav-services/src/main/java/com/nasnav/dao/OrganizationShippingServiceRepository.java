package com.nasnav.dao;

import com.nasnav.persistence.OrganizationShippingServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationShippingServiceRepository extends JpaRepository<OrganizationShippingServiceEntity, Long> {
	List<OrganizationShippingServiceEntity> getByOrganization_Id(Long orgId);

	Optional<OrganizationShippingServiceEntity> getByOrganization_IdAndServiceId(Long orgId, String serviceId);
}
