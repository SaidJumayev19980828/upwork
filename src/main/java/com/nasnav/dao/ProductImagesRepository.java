package com.nasnav.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.nasnav.persistence.ProductImagesEntity;

public interface ProductImagesRepository extends CrudRepository<ProductImagesEntity, Long>{

	List<ProductImagesEntity> findByProductEntity_Id(Long productId);
	
	List<ProductImagesEntity> findByProductVariantsEntity_Id(Long productVariantId);
}
