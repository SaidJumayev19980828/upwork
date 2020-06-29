package com.nasnav.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nasnav.persistence.ProductExtraAttributesEntity;
import com.nasnav.persistence.dto.query.result.products.export.VariantExtraAtrribute;
import org.springframework.transaction.annotation.Transactional;

public interface ProductExtraAttributesEntityRepository extends JpaRepository<ProductExtraAttributesEntity, Long>{
	
	@Query("SELECT NEW com.nasnav.persistence.dto.query.result.products.export.VariantExtraAtrribute(variant.id, attr.id, attr_def.name, attr.value)"
			+ " FROM StocksEntity stock "
			+ " JOIN stock.productVariantsEntity variant "
			+ " LEFT JOIN variant.extraAttributes attr "
			+ " JOIN attr.extraAttribute attr_def"
			+ " WHERE stock.shopsEntity.id = :shopId")
	List<VariantExtraAtrribute> findByVariantShopId(@Param("shopId")Long shopId);





	@Transactional
	@Modifying
	@Query(value = "delete from ProductExtraAttributesEntity pea where pea.extraAttribute.id = :attrId " +
			"and pea.variant in (select v from ProductVariantsEntity v where v.productEntity.organizationId = :orgId)")
	void deleteByIdAndOrganizationId(@Param("attrId") Integer attrId, @Param("orgId") Long orgId);

}
