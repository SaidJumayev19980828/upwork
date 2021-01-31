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


	@Query("select i from ProductImagesEntity i " +
			" left join fetch i.productEntity p " +
			" left join fetch i.productVariantsEntity v"+
			" where i.uri in :uris")
	List<ProductImagesEntity> findByUriIn(@Param("uris") List<String> uris);

	boolean existsByUri(String uri);

	void deleteByProductEntity_Id(Long productId);

	@Transactional
    @Modifying
	void deleteByProductEntity_IdAndProductEntity_organizationId(Long productId, Long orgId);

	List<ProductImagesEntity> findByProductEntity_IdAndPriorityOrderByPriority(Long productId, Integer priority);

	Long countByUri(String uri);

	List<ProductImagesEntity> findByPriorityAndProductEntity_IdInOrderByPriority(int priority, List<Long> productIds);

	List<ProductImagesEntity> findByProductEntity_IdInOrderByPriority(List<Long> productIds);

	@Query(value = "select i from ProductImagesEntity i" +
			" left join fetch i.productVariantsEntity v left join fetch i.productEntity p"+
			" where p.id in :productIds or v.productEntity.id in :productIds order by i.priority")
	List<ProductImagesEntity> findByProductsIds(@Param("productIds") List<Long> productIds);

	List<ProductImagesEntity> findByProductVariantsEntity_IdInOrderByPriority(Set<Long> variandIds);


	@Query("SELECT img FROM ProductImagesEntity img " +
			" LEFT JOIN FETCH img.productEntity prod " +
			" LEFT JOIN FETCH img.productVariantsEntity variant " +
			" LEFT JOIN variant.productEntity variantProd " +
			" WHERE prod.organizationId = :orgId OR variantProd.organizationId = :orgId")
	List<ProductImagesEntity> findByProductEntity_OrganizationId(@Param("orgId")Long orgId);

	
	@Query(value = "DELETE from Product_Images img where img.product_id in(select id from products product where product.organization_id = :orgId)"
			, nativeQuery = true)
	@Transactional
    @Modifying
	void deleteByProductEntity_organizationId(@Param("orgId") Long orgId);

	Long countByProductEntity_OrganizationId(long l);

	Set<ProductImagesEntity> findByProductEntity_IdAndTypeOrderByPriority(Long productId, int productImage);



	@Query(value = "SELECT new com.nasnav.dto.ProductImageDTO(i.id, i.uri, i.productEntity.id, i.productVariantsEntity.id, i.priority)" +
				" from ProductImagesEntity i "+ 
				" where i.productEntity.id in (:productsIds) "+
				"   or i.productVariantsEntity.id in (:variantsIds)" +
			    " order by "
			    + "  case when i.productVariantsEntity is null "
			    + "  then 0 else 1 "
			    + "  end"
			    + " , i.priority")
	List<ProductImageDTO> getProductsAndVariantsImages(@Param("productsIds") List<Long> productsIds,
													   @Param("variantsIds") List<Long> variantsIds);

	
	@Query(value = "SELECT new com.nasnav.dto.ProductImageDTO(i.id, i.uri, i.productEntity.id, i.productVariantsEntity.id, i.priority)" +
			" from ProductImagesEntity i "
			+ " where i.productEntity.id in (:productsIds) or i.productVariantsEntity.id in (:variantsIds)" +
			" order by case when i.productVariantsEntity is null then 1 else 0 end, i.priority")
	List<ProductImageDTO> getProductsAndVariantsImagesOrderedByVariantImgsFirst(@Param("productsIds") List<Long> productsIds,
													   @Param("variantsIds") List<Long> variantsIds);

}
