package com.nasnav.dao;

import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.service.model.VariantBasicData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductVariantsRepository extends JpaRepository<ProductVariantsEntity, Long>{

	@Query("select variant from ProductVariantsEntity variant " +
			"left join fetch variant.featureValues value " +
			"left join fetch value.feature feature " +
			"where variant.id = :variantId")
	ProductVariantsEntity findByVariantId(@Param("variantId") Long variantId);

	@Query("select distinct v from ProductVariantsEntity v" +
			" left join fetch v.featureValues featureValue " +
			" left join fetch featureValue.feature feature " +
			" left join fetch v.productEntity p " +
			" left join fetch v.stocks s " +
			" where p.id = :id")
	List<ProductVariantsEntity> findByProductEntity_Id(@Param("id") Long productId);

	List<ProductVariantsEntity> findByBarcodeAndProductEntity_OrganizationId(String barcode, Long orgId);

	Optional<ProductVariantsEntity> findByIdAndProductEntity_OrganizationId(Long id, Long orgId);

	List<ProductVariantsEntity> findDistinctByIdInAndProductEntity_OrganizationId(List<Long> ids, Long orgId);


	@Query("SELECT variant FROM ProductVariantsEntity variant INNER JOIN FETCH variant.productEntity prod where prod.organizationId = :orgId")
	List<ProductVariantsEntity> findByOrganizationId(@Param("orgId") Long orgId);

	@Query("SELECT variant FROM ProductVariantsEntity variant INNER JOIN FETCH variant.productEntity prod " +
			"where prod.organizationId = :orgId and variant.removed = 0 and " +
			"(variant.barcode = :name or LOWER(variant.name) like %:name% or LOWER(variant.description) like %:name%) "+
			"order by variant.name ")
	List<ProductVariantsEntity> findByOrganizationId(@Param("orgId") Long orgId,
													 @Param("name") String name,
													 Pageable pageable);

	@Query("SELECT variant FROM ProductVariantsEntity variant " +
			" INNER JOIN FETCH variant.productEntity prod " +
			" INNER JOIN OrganizationEntity org on prod.organizationId = org.id " +
			" where org.yeshteryState = 1 and variant.removed = 0 and " +
			" ( LOWER(variant.barcode) like %:name% or LOWER(variant.name) like %:name% or LOWER(variant.description) like %:name%) "+
			" order by variant.name ")
	List<ProductVariantsEntity> findByYeshteryProducts(@Param("name") String name, Pageable pageable);

	@Query("SELECT NEW com.nasnav.service.model.VariantBasicData(variant.id, variant.productEntity.id, variant.productEntity.organizationId, variant.barcode) "
			+ " FROM ProductVariantsEntity variant "
			+ " where variant.id in :idList and variant.productEntity.removed = 0")
	List<VariantBasicData> findVariantBasicDataByIdIn(@Param("idList") List<Long> idList);

	@Query("select distinct variant from ProductVariantsEntity variant " +
			" left join fetch variant.featureValues featureValues " +
			" left join fetch featureValues.feature feature " +
			" where variant.id in :idList")
	List<ProductVariantsEntity> findByIdIn(@Param("idList") List<Long> idList);
	
	@Query("SELECT NEW com.nasnav.service.model.VariantBasicData(variant.id, variant.productEntity.id, variant.productEntity.organizationId, variant.barcode) "
			+ " FROM ProductVariantsEntity variant "
			+ " where variant.productEntity.organizationId = :orgId "
			+ " AND variant.barcode in (:barcodeList)"
			+ " AND variant.productEntity.removed = 0")
	List<VariantBasicData> findByOrganizationIdAndBarcodeIn(@Param("orgId") Long orgId,  @Param("barcodeList") List<String> barcodeList);

	long countByProductEntity_organizationId(long l);

	Long countByIdInAndProductEntity_organizationId(List<Long> ids, Long orgId);


	@Transactional
    @Modifying
    @Query( value = "update product_variants " + 
    		" set removed = 1 " + 
    		" where product_id in " + 
    		" (select id from products prod where prod.organization_id = :orgId)", nativeQuery = true )
	void deleteAllByProductEntity_organizationId(@Param("orgId")Long orgId);

	@Query(value = "select distinct variant from ProductVariantsEntity variant"
					+" inner join variant.stocks stock"
					+" left join fetch variant.featureValues featureValues"
					+" left join fetch featureValues.feature feature"
					+" where stock.id in :stockIds and variant.removed = 0")
	List<ProductVariantsEntity> findByStockIdIn(@Param("stockIds") List<Long> stockIds);
	
	@Query("select variant.id from ProductVariantsEntity variant where variant.productEntity.id in :idList")
	Set<Long> findVariantIdByProductIdIn(@Param("idList") List<Long> productIdList);
	
	@Transactional
    @Modifying
    @Query( value = "update ProductVariantsEntity variant set variant.removed = 1 where variant.productEntity.id in :idList")
	void deleteAllByProductIdIn(@Param("idList") List<Long> idList);

	@Transactional
	@Modifying
	@Query( value = "update ProductVariantsEntity variant set variant.removed = 1 where variant.id in :idList")
	void deleteByIdIn(@Param("idList") List<Long> idList);

	
	@Query("SELECT variant "
			+ " FROM ProductVariantsEntity variant "
			+ " left join fetch variant.featureValues featureValue "
			+ " left join fetch featureValue.feature feature"
			+ " left join fetch variant.productEntity product "
			+ " left join fetch variant.stocks stocks "
			+ " left join fetch variant.extraAttributes attr"
			+ " WHERE variant.id = :id")
	Optional<ProductVariantsEntity> getVariantFullData(@Param("id") Long id);

	@Query(value = "select v.id from StocksEntity s inner join ProductVariantsEntity v on v.id = s.productVariantsEntity.id " +
				   " where s.id in :stocksIds")
	List<Long> getVariantsIdsByStocksIds(@Param("stocksIds") List<Long> stocksIds);

	@Query("SELECT count(variant.id) FROM ProductVariantsEntity variant " +
			" INNER JOIN variant.productEntity prod " +
			" where prod.organizationId = :orgId and variant.removed = 0 and " +
			"(variant.barcode = :name or LOWER(variant.name) like %:name% or LOWER(variant.description) like %:name%)" +
			"")
	Long countByOrganizationId(@Param("orgId") Long orgId,
													 @Param("name") String name);

	@Query("SELECT count(variant.id) FROM ProductVariantsEntity variant " +
			" INNER JOIN variant.productEntity prod " +
			" INNER JOIN OrganizationEntity org on prod.organizationId = org.id " +
			" where org.yeshteryState = 1 and variant.removed = 0 and " +
			"(LOWER(variant.barcode) like %:name% or LOWER(variant.name) like %:name% or LOWER(variant.description) like %:name%)" )
	Long countByYeshteryProducts(@Param("name") String name);

    List<ProductVariantsEntity> findByIdInAndProductEntity_OrganizationId(List<Long> ids, Long orgId);

	@Query("select feature.id from ProductVariantsEntity variant " +
			" left join variant.featureValues featureValues " +
			" left join featureValues.feature feature " +
			" left join variant.productEntity product " +
			" where feature.id = :featureId and product.organizationId = :orgId")
	List<Long> findByFeature(@Param("featureId")Integer featureId, @Param("orgId") Long orgId);
}
