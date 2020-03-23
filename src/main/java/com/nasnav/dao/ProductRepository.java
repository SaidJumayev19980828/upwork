package com.nasnav.dao;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.dto.Pair;
import com.nasnav.persistence.ProductEntity;

public interface ProductRepository extends CrudRepository<ProductEntity,Long> {

    List<ProductEntity> findByOrganizationId(Long organizationId);
    List<ProductEntity> findByOrganizationIdOrderByIdAsc(Long organizationId);
    List<ProductEntity> findByOrganizationIdOrderByIdDesc(Long organizationId);

    List<ProductEntity> findByOrganizationIdOrderByNameAsc(Long organizationId);
    List<ProductEntity> findByOrganizationIdOrderByNameDesc(Long organizationId);

    List<ProductEntity> findByOrganizationIdOrderByPnameAsc(Long organizationId);
    List<ProductEntity> findByOrganizationIdOrderByPnameDesc(Long organizationId);


    List<ProductEntity> findByOrganizationIdAndBrandId(Long organizationId, Long brandId);
    List<ProductEntity> findByOrganizationIdAndBrandIdOrderByIdAsc(Long organizationId,Long brandId);
    List<ProductEntity> findByOrganizationIdAndBrandIdOrderByIdDesc(Long organizationId,Long brandId);
    List<ProductEntity> findByOrganizationIdAndBrandIdOrderByNameAsc(Long organizationId,Long brandId);
    List<ProductEntity> findByOrganizationIdAndBrandIdOrderByNameDesc(Long organizationId,Long brandId);
    List<ProductEntity> findByOrganizationIdAndBrandIdOrderByPnameAsc(Long organizationId,Long brandId);
    List<ProductEntity> findByOrganizationIdAndBrandIdOrderByPnameDesc(Long organizationId,Long brandId);

    List<ProductEntity> findByIdIn(List<Long> ids);
    List<ProductEntity> findByIdInOrderByIdAsc(List<Long> ids);
    List<ProductEntity> findByIdInOrderByIdDesc(List<Long> ids);
    List<ProductEntity> findByIdInOrderByNameAsc(List<Long> ids);
    List<ProductEntity> findByIdInOrderByNameDesc(List<Long> ids);
    List<ProductEntity> findByIdInOrderByPnameAsc(List<Long> ids);
    List<ProductEntity> findByIdInOrderByPnameDesc(List<Long> ids);

//    List<ProductEntity> findByIdInOrderByPriceAsc(List<Long> ids);
//    List<ProductEntity> findByIdInOrderByPriceDesc(List<Long> ids);

    List<ProductEntity> findByIdInAndBrandId(List<Long> ids, Long brandId);
    List<ProductEntity> findByIdInAndBrandIdOrderByIdAsc(List<Long> ids, Long brandId);
    List<ProductEntity> findByIdInAndBrandIdOrderByIdDesc(List<Long> ids, Long brandId);
    List<ProductEntity> findByIdInAndBrandIdOrderByNameAsc(List<Long> ids, Long brandId);
    List<ProductEntity> findByIdInAndBrandIdOrderByNameDesc(List<Long> ids, Long brandId);
    List<ProductEntity> findByIdInAndBrandIdOrderByPnameAsc(List<Long> ids, Long brandId);
    List<ProductEntity> findByIdInAndBrandIdOrderByPnameDesc(List<Long> ids, Long brandId);

//    List<ProductEntity> findByIdInAndCategoryIdOrderByPriceAsc(List<Long> ids,Long categoryId);
//    List<ProductEntity> findByIdInAndCategoryIdOrderByPriceDesc(List<Long> ids,Long categoryId);
	
	Optional<ProductEntity> findByBarcodeAndOrganizationId(String barcode, Long orgId);
	Optional<ProductEntity> findByName(String name);


	@Query("SELECT products FROM ProductEntity products "
			+ " LEFT JOIN FETCH products.productVariants variants "
			+ " LEFT JOIN FETCH variants.stocks stocks"
			+ " LEFT JOIN FETCH stocks.organizationEntity"
			+ " WHERE products.id in :productIdList")
	Set<ProductEntity> findFullDataByIdIn(@Param("productIdList") List<Long> productIdList);


    @Query(value = "SELECT t.product_id FROM Product_tags t WHERE t.tag_id in :tagsIds", nativeQuery = true)
    List<Long> getProductIdsByTagsList(@Param("tagsIds") List<Long> tagsIds);


    @Query(nativeQuery = true)
    List<Pair> getProductTags(@Param("productsIds") List<Long> productsIds, @Param("tagsIds") List<Long> tagsIds);

    @Query(value = "SELECT t.tag_id FROM Product_tags t WHERE t.tag_id in (select tag_id from tags where organization_id = :orgId)", nativeQuery = true)
    List<BigInteger> getUsedTagsByOrg(@Param("orgId") Long orgId);


    @Query(value = "select t.tag_id from Product_tags t where t.product_id = :id", nativeQuery = true)
    List<BigInteger> getTagsByProductId(@Param("id") Long id);

    @Query(nativeQuery = true)
    List<Pair> getTagsByProductIdIn(@Param("ids") List<Long> id);

    @Query(value = "delete from Product_tags where tag_id = :tag_id", nativeQuery = true)
    @Transactional
    @Modifying
    void detachProductsFromTag(@Param("tag_id") Long tagId);

	List<ProductEntity> findByNameAndOrganizationId(String name, Long orgId);
	
	long countByOrganizationId(long l);
	
	

	@Transactional
    @Modifying
    @Query( value = "update public.products set removed = 1 where organization_Id = :orgId", nativeQuery = true )
	void deleteAllByOrganizationId(@Param("orgId")Long orgId);
}



