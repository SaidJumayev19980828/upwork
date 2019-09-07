package com.nasnav.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.nasnav.persistence.ProductFeaturesEntity;

public interface ProductFeaturesRepository extends CrudRepository<ProductFeaturesEntity, Integer>{

	List<ProductFeaturesEntity> findByOrganizationId(Long orgId);

}
