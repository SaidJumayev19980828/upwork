package com.nasnav.dao;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.nasnav.persistence.ProductImagesEntity;
import org.springframework.data.repository.query.Param;

public interface ProductImagesRepository extends CrudRepository<ProductImagesEntity, Long>{

	List<ProductImagesEntity> findByProductEntity_Id(Long productId);
	
	List<ProductImagesEntity> findByProductVariantsEntity_Id(Long productVariantId);
	
	Optional<ProductImagesEntity> findByUri(String uri);

	void deleteByProductEntity_Id(Long productId);

	List<ProductImagesEntity> findByProductEntity_IdOrderByPriority(Long productId);

	Long countByUri(String uri);

	List<ProductImagesEntity> findByPriorityAndProductEntity_IdInOrderByPriority(int priority, List<Long> productIds);

	List<ProductImagesEntity> findByProductEntity_IdInOrderByPriority(List<Long> productIds);

	@Query(value = "select i.id, i.uri, i.type, i.priority, i.product_id, i.variant_id from Product_Images i left join product_variants v on i.variant_id = v.id"+
			" where i.product_id in :productIds or v.product_id in :productIds order by i.priority", nativeQuery = true)
	List<ProductImagesEntity> findByProductsIds(@Param("productIds") List<Long> productIds);

	List<ProductImagesEntity> findByProductVariantsEntity_IdInOrderByPriority(Set<Long> variandIds);
}
