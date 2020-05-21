package com.nasnav.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nasnav.persistence.ProductExtraAttributesEntity;
import com.nasnav.persistence.dto.query.result.products.export.VariantExtraAtrribute;

public interface ProductExtraAttributesEntityRepository extends JpaRepository<ProductExtraAttributesEntity, Long>{
	
	@Query("SELECT NEW com.nasnav.persistence.dto.query.result.products.export.VariantExtraAtrribute(variant.id, attr.id, attr_def.name, attr.value)"
			+ " FROM StocksEntity stock "
			+ " JOIN stock.productVariantsEntity variant "
			+ " LEFT JOIN variant.extraAttributes attr "
			+ " JOIN attr.extraAttribute attr_def"
			+ " WHERE stock.shopsEntity.id = :shopId")
	List<VariantExtraAtrribute> findByVariantShopId(@Param("shopId")Long shopId);
}
