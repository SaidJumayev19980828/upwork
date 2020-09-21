package com.nasnav.dao;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.service.model.VariantBasicData;

public interface ProductVariantsRepository extends JpaRepository<ProductVariantsEntity, Long>{

	List<ProductVariantsEntity> findByProductEntity_Id(Long productId);

	List<ProductVariantsEntity> findByProductEntity_IdIn(List<Long> productIdsList);

	ProductVariantsEntity findByIdAndProductEntity_Id(Long variantId, Long productId);

	void deleteByProductEntity_Id(Long productId);

	List<ProductVariantsEntity> findByBarcodeAndProductEntity_OrganizationId(String barcode, Long orgId);

	Optional<ProductVariantsEntity> findByIdAndProductEntity_OrganizationId(Long id, Long orgId);

	List<ProductVariantsEntity> findByIdInAndProductEntity_OrganizationId(List<Long> ids, Long orgId);


	@Query("SELECT variant FROM ProductVariantsEntity variant INNER JOIN FETCH variant.productEntity prod where prod.organizationId = :orgId")
	List<ProductVariantsEntity> findByOrganizationId(@Param("orgId") Long orgId);

	@Query("SELECT variant FROM ProductVariantsEntity variant INNER JOIN FETCH variant.productEntity prod " +
			"where prod.organizationId = :orgId and " +
			"(variant.barcode = :name or LOWER(variant.name) like %:name% or LOWER(variant.description) like %:name%) "+
			"order by variant.name ")
	List<ProductVariantsEntity> findByOrganizationId(@Param("orgId") Long orgId,
													 @Param("name") String name,
													 Pageable pageable);

	@Query("SELECT NEW com.nasnav.service.model.VariantBasicData(variant.id, variant.productEntity.id, variant.productEntity.organizationId, variant.barcode) "
			+ " FROM ProductVariantsEntity variant "
			+ " where variant.id in :idList")
	List<VariantBasicData> findVariantBasicDataByIdIn(@Param("idList") List<Long> idList);
	
	List<ProductVariantsEntity> findByIdIn(List<Long> idList);
	
	@Query("SELECT NEW com.nasnav.service.model.VariantBasicData(variant.id, variant.productEntity.id, variant.productEntity.organizationId, variant.barcode) "
			+ " FROM ProductVariantsEntity variant "
			+ " where variant.productEntity.organizationId = :orgId "
			+ " AND variant.barcode in (:barcodeList)")
	List<VariantBasicData> findByOrganizationIdAndBarcodeIn(@Param("orgId") Long orgId,  @Param("barcodeList") List<String> barcodeList);

	long countByProductEntity_organizationId(long l);

	long countByProductEntity_organizationIdAndNameContainingIgnoreCase(long l, String name);

	
	@Transactional
    @Modifying
    @Query( value = "update product_variants " + 
    		" set removed = 1 " + 
    		" where product_id in " + 
    		" (select id from products prod where prod.organization_id = :orgId)", nativeQuery = true )
	void deleteAllByProductEntity_organizationId(@Param("orgId")Long orgId);

	
	@Query("select variant.id from ProductVariantsEntity variant where variant.id in :idList")
	Set<Long> findExistingVariantsByIdIn(@Param("idList") List<Long> variantIdList);

	@Query("select variant.id from ProductVariantsEntity variant where variant.productEntity.organizationId = :orgId")
	Set<Long> listVariantIdByOrganizationId(@Param("orgId")Long orgId);
	
	@Query("select variant.id from ProductVariantsEntity variant where variant.productEntity.id in :idList")
	Set<Long> findVariantIdByProductIdIn(@Param("idList") List<Long> productIdList);
	
	@Transactional
    @Modifying
    @Query( value = "update public.product_variants set removed = 1 where product_id in :idList", nativeQuery = true )
	void deleteAllByProductIdIn(@Param("idList") List<Long> idList);
	
	
	@Query("SELECT variant "
			+ " FROM ProductVariantsEntity variant "
			+ " left join fetch variant.productEntity product "
			+ " left join fetch variant.stocks stocks "
			+ " left join fetch variant.extraAttributes attr"
			+ " WHERE variant.id = :id")
	Optional<ProductVariantsEntity> getVariantFullData(@Param("id") Long id);

	@Query(value = "select v.id from StocksEntity s inner join ProductVariantsEntity v on v.id = s.productVariantsEntity.id " +
				   " where s.id in :stocksIds")
	List<Long> getVariantsIdsByStocksIds(@Param("stocksIds") List<Long> stocksIds);

}
