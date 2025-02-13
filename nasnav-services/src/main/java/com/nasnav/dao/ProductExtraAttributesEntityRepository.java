package com.nasnav.dao;

import com.nasnav.persistence.ExtraAttributesEntity;
import com.nasnav.persistence.ProductExtraAttributesEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.dto.query.result.products.export.VariantExtraAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductExtraAttributesEntityRepository extends JpaRepository<ProductExtraAttributesEntity, Long>{
	
	@Query("SELECT NEW com.nasnav.persistence.dto.query.result.products.export.VariantExtraAttribute(variant.id, attr.id, attr_def.name, attr.value)"
			+ " FROM StocksEntity stock "
			+ " JOIN stock.productVariantsEntity variant "
			+ " LEFT JOIN variant.extraAttributes attr "
			+ " JOIN attr.extraAttribute attr_def"
			+ " WHERE stock.shopsEntity.id = :shopId")
	List<VariantExtraAttribute> findByVariantShopId(@Param("shopId")Long shopId);


	@Query("SELECT NEW com.nasnav.persistence.dto.query.result.products.export.VariantExtraAttribute(variant.id, attr.id, attr_def.name, attr.value)"
			+ " FROM ProductVariantsEntity variant "
			+ " LEFT JOIN variant.productEntity product "
			+ " LEFT JOIN variant.extraAttributes attr "
			+ " JOIN attr.extraAttribute attr_def"
			+ " WHERE product.organizationId = :orgId")
	List<VariantExtraAttribute> findByVariantOrgId(@Param("orgId")Long orgId);


	ProductExtraAttributesEntity findByIdAndExtraAttribute_IdAndVariant_Id(Long id, Integer extraAttrId, Long variantId);

	@Transactional
	@Modifying
	@Query("delete from ProductExtraAttributesEntity pea where pea.id = :variantExtraAttributeId and pea.variant = :variant and pea.extraAttribute = :extraAttribute")
	void deleteByIdVariantAndExtraAttribute(@Param("variantExtraAttributeId") Long variantExtraAttributeId,
											@Param("variant") ProductVariantsEntity variant,
											@Param("extraAttribute") ExtraAttributesEntity extraAttribute);

	@Transactional
	@Modifying
	@Query("delete from ProductExtraAttributesEntity pea where pea.variant = :variant and pea.extraAttribute = :extraAttribute")
	void deleteByIdVariantAndExtraAttribute(@Param("variant") ProductVariantsEntity variant,
											@Param("extraAttribute") ExtraAttributesEntity extraAttribute);

	@Transactional
	@Modifying
	@Query(value = "delete from ProductExtraAttributesEntity pea "
			+ " where pea.extraAttribute.id = :attrId " 
			+ " and pea.extraAttribute in "
			+ "   (select extra from ExtraAttributesEntity extra"
			+ "    where extra.organizationId = :orgId)")
	void deleteByIdAndOrganizationId(@Param("attrId") Integer attrId, @Param("orgId") Long orgId);

    List<ProductExtraAttributesEntity> findByExtraAttribute_NameAndVariantIdIn(String name, List<Long> variants);

	@Transactional
	@Modifying
	@Query(value = "delete from ProductExtraAttributesEntity pea "
			+ " where  pea.extraAttribute in "
			+ "   (select extra from ExtraAttributesEntity extra"
			+ "    where extra.organizationId = :orgId"
			+ "		and extra.name = :name)")
    void deleteByNameAndOrganizationId(@Param("name")String extraAttrName, @Param("orgId")Long orgId);

	@Query("SELECT val FROM ProductExtraAttributesEntity val " +
			" LEFT JOIN FETCH val.variant variant " +
			" LEFT JOIN FETCH val.extraAttribute attr" +
			" WHERE attr = :extraAttribute " +
			" AND variant is NOT NULL ")
	List<ProductExtraAttributesEntity> findByExtraAttribute(@Param("extraAttribute")ExtraAttributesEntity extraAttribute);
}
