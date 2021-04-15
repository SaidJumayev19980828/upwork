package com.nasnav.dao;

import com.nasnav.persistence.OrganizationCartOptimizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrganizationCartOptimizationRepository extends JpaRepository<OrganizationCartOptimizationEntity, Long> {

	Optional<OrganizationCartOptimizationEntity> findByOptimizationStrategyAndOrganization_Id(String strategy, long l);

	Optional<OrganizationCartOptimizationEntity> findByOptimizationStrategyAndShippingServiceIdAndOrganization_Id(String strategy,
			String shippingServiceId, Long id);

	
	@Query("SELECT optimize "
			+ " FROM OrganizationCartOptimizationEntity optimize "
			+ " LEFT JOIN optimize.organization org "
			+ " WHERE org.id = :orgId "
			+ " AND optimize.shippingServiceId IS NULL ")
	Optional<OrganizationCartOptimizationEntity> findOrganizationDefaultOptimizationStrategy(@Param("orgId")Long orgId);

	Optional<OrganizationCartOptimizationEntity> findByShippingServiceIdAndOrganization_Id(String shippingServiceId, Long orgId);

	List<OrganizationCartOptimizationEntity> findByOrganization_Id(Long orgId);
}
