package com.nasnav.dao;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.repository.CrudRepository;

import com.nasnav.persistence.ProductImagesEntity;

public interface ProductImagesRepository extends CrudRepository<ProductImagesEntity, Long>{

	List<ProductImagesEntity> findByProductEntity_Id(Long productId);
	
	List<ProductImagesEntity> findByProductVariantsEntity_Id(Long productVariantId);
	
	Optional<ProductImagesEntity> findByUri(String uri);

	void deleteByProductEntity_Id(Long productId);

	List<ProductImagesEntity> findByProductEntity_IdOrderByPriority(Long productId);

	Long countByUri(String uri);

	List<ProductImagesEntity> findByPriorityAndProductEntity_IdInOrderByPriority(int priority, List<Long> productIds);

	List<ProductImagesEntity> findByProductEntity_IdInOrderByPriority(List<Long> productIds);
	List<ProductImagesEntity> findByProductVariantsEntity_IdInOrderByPriority(Set<Long> variandIds);
}
