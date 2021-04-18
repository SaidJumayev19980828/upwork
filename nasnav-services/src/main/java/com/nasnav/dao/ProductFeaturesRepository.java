package com.nasnav.dao;

import com.nasnav.persistence.ProductFeaturesEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductFeaturesRepository extends CrudRepository<ProductFeaturesEntity, Integer>{

	List<ProductFeaturesEntity> findByOrganizationId(Long orgId);

	@Query("SELECT f FROM ProductFeaturesEntity f "
			+ " JOIN f.organization org "
			+ " JOIN ShopsEntity shop on shop.organizationEntity = org "
			+ " WHERE shop.id = :shopId")
	List<ProductFeaturesEntity> findByShopId(@Param("shopId") Long shopId);

    boolean existsByIdAndOrganization_Id(Integer featureId, Long orgId);


	@Query("SELECT f FROM ProductFeaturesEntity f "
			+ " LEFT JOIN FETCH f.organization org "
			+ " WHERE f.id = :featureId AND org.id = :orgId ")
	Optional<ProductFeaturesEntity> findByIdAndOrganization_Id(Integer featureId, Long orgId);

    boolean existsByNameAndOrganizationId(String name, Long orgId);
}
