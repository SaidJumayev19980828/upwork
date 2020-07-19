package com.nasnav.dao;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.nasnav.dto.ProductImageDTO;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.persistence.ProductImagesEntity;

public interface ProductImagesRepository extends CrudRepository<ProductImagesEntity, Long>{

	List<ProductImagesEntity> findByProductEntity_Id(Long productId);

	List<ProductImagesEntity> findByProductEntity_IdIn(List<Long> productIds);
	
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

	
	@Query(value = "DELETE from Product_Images img where img.product_id in(select id from products product where product.organization_id = :orgId)"
			, nativeQuery = true)
	@Transactional
    @Modifying
	void deleteByProductEntity_organizationId(@Param("orgId") Long orgId);

	Long countByProductEntity_OrganizationId(long l);

	Set<ProductImagesEntity> findByProductEntity_IdAndTypeOrderByPriority(Long productId, int productImage);



	@Query(value = "SELECT new com.nasnav.dto.ProductImageDTO(i.id, i.uri, i.productEntity.id, i.productVariantsEntity.id, i.priority)" +
			" from ProductImagesEntity i where i.productEntity.id in (:productsIds) or i.productVariantsEntity.id in (:variantsIds)" +
			" order by case when i.productVariantsEntity is null then 0 else 1 end, i.priority")
	List<ProductImageDTO> getProductsAndVariantsImages(@Param("productsIds") List<Long> productsIds,
													   @Param("variantsIds") List<Long> variantsIds);

}
