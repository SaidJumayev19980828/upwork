package com.nasnav.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.nasnav.persistence.ProductFeaturesEntity;

public interface ProductFeaturesRepository extends CrudRepository<ProductFeaturesEntity, Integer>{

	List<ProductFeaturesEntity> findByOrganizationId(Long orgId);

	@Query("SELECT f FROM ProductFeaturesEntity f "
			+ " JOIN f.organization org "
			+ " JOIN ShopsEntity shop on shop.organizationEntity = org "
			+ " WHERE shop.id = :shopId")
	List<ProductFeaturesEntity> findByShopId(@Param("shopId") Long shopId);

    boolean existsByIdAndOrganization_Id(Integer featureId, Long orgId);

	Optional<ProductFeaturesEntity> findByIdAndOrganization_Id(Integer featureId, Long orgId);
}
