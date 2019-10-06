package com.nasnav.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.ProductVariantsEntity;

public interface ProductVariantsRepository extends JpaRepository<ProductVariantsEntity, Long>{

	List<ProductVariantsEntity> findByProductEntity_Id(Long productId);

	List<ProductVariantsEntity> findByProductEntity_IdIn(List<Long> productIdsList);

	ProductVariantsEntity findByIdAndProductEntity_Id(Long variantId, Long productId);

	void deleteByProductEntity_Id(Long productId);

	Optional<ProductVariantsEntity> findByBarcodeAndProductEntity_OrganizationId(String barcode, Long orgId);
}
