package com.nasnav.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.persistence.ProductVariantsEntity;

public interface ProductVariantsRepository extends JpaRepository<ProductVariantsEntity, Long>{

	List<ProductVariantsEntity> findByProductEntity_Id(Long productId);

	List<ProductVariantsEntity> findByProductEntity_IdIn(List<Long> productIdsList);

	ProductVariantsEntity findByIdAndProductEntity_Id(Long variantId, Long productId);

	void deleteByProductEntity_Id(Long productId);

	List<ProductVariantsEntity> findByBarcodeAndProductEntity_OrganizationId(String barcode, Long orgId);

	Optional<ProductVariantsEntity> findByIdAndProductEntity_OrganizationId(Long id, Long orgId);

	@Query("SELECT variant FROM ProductVariantsEntity variant INNER JOIN FETCH variant.productEntity prod where prod.organizationId = :orgId")
	List<ProductVariantsEntity> findByOrganizationId(@Param("orgId") Long orgId);
	
	List<ProductVariantsEntity> findByIdIn(List<Long> idList);
	
	@Query("SELECT variant FROM ProductVariantsEntity variant INNER JOIN FETCH variant.productEntity prod where prod.organizationId = :orgId "
			+ " AND variant.barcode in (:barcodeList)")
	List<ProductVariantsEntity> findByOrganizationIdAndBarcodeIn(@Param("orgId") Long orgId,  @Param("barcodeList") List<String> barcodeList);

	long countByProductEntity_organizationId(long l);

	
	@Transactional
    @Modifying
    @Query( value = "update product_variants " + 
    		" set removed = 1 " + 
    		" where product_id in " + 
    		" (select id from products prod where prod.organization_id = :orgId)", nativeQuery = true )
	void deleteAllByProductEntity_organizationId(@Param("orgId")Long orgId);
}
