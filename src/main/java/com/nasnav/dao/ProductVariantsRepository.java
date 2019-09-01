package com.nasnav.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.nasnav.persistence.ProductVariantsEntity;

public interface ProductVariantsRepository extends CrudRepository<ProductVariantsEntity, Long>{

	List<ProductVariantsEntity> findByProductEntity_Id(Long productId);

	ProductVariantsEntity findByIdAndProductEntity_Id(Long variantId, Long productId);
}
