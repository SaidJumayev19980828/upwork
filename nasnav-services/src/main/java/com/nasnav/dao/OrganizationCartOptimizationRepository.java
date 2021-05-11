package com.nasnav.dao;

import com.nasnav.persistence.OrganizationCartOptimizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface OrganizationCartOptimizationRepository extends JpaRepository<OrganizationCartOptimizationEntity, Long> {

	Optional<OrganizationCartOptimizationEntity> findFirstByOptimizationStrategyAndOrganization_IdOrderByIdDesc(String strategy, long orgId);

	Optional<OrganizationCartOptimizationEntity> findFirstByOptimizationStrategyAndShippingServiceIdAndOrganization_IdOrderByIdDesc(String strategy,
																																	String shippingServiceId, Long orgId);

	
	@Query("SELECT optimize "
			+ " FROM OrganizationCartOptimizationEntity optimize "
			+ " LEFT JOIN optimize.organization org "
			+ " WHERE org.id = :orgId "
			+ " AND optimize.shippingServiceId IS NULL ")
	Optional<OrganizationCartOptimizationEntity> findOrganizationDefaultOptimizationStrategy(@Param("orgId")Long orgId);

	Optional<OrganizationCartOptimizationEntity> findByShippingServiceIdAndOrganization_Id(String shippingServiceId, Long orgId);

	List<OrganizationCartOptimizationEntity> findByOrganization_Id(Long orgId);

	@Transactional
	@Modifying
	@Query("DELETE FROM OrganizationCartOptimizationEntity opt " +
			" WHERE opt.optimizationStrategy = :strategyName " +
			" AND opt.shippingServiceId = :shippingService " +
			" AND opt.organization.id = :orgId")
	void deleteByOptimizationStrategy(
			@Param("strategyName")String strategyName
			, @Param("shippingService")String shippingService
			, @Param("orgId")Long orgId);

	@Transactional
	@Modifying
	@Query("DELETE FROM OrganizationCartOptimizationEntity opt " +
			" WHERE opt.optimizationStrategy = :strategyName " +
			" AND opt.shippingServiceId IS NULL " +
			" AND opt.organization.id = :orgId")
	void deleteByOptimizationStrategy(
			@Param("strategyName")String strategyName
			, @Param("orgId")Long orgId);
}
