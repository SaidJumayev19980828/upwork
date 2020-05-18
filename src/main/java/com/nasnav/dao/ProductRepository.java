package com.nasnav.dao;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.nasnav.dto.response.navbox.ThreeSixtyProductsDTO;
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
			+ " LEFT JOIN FETCH products.tags tags "
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

    @Query(value = "select t.name from tags t where t.id in (select pt.tag_id from Product_tags pt where pt.product_id = :id)"
            , nativeQuery = true)
    List<String> getTagsNamesByProductId(@Param("id") Long id);

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
	
	
	@Transactional
    @Modifying
    @Query( value = "update public.products set removed = 1 where id in :idList", nativeQuery = true )
	void deleteAllByIdIn(@Param("idList") List<Long> idList);
	
	
	@Query("select products.id FROM ProductEntity products where products.organizationId = :orgId")
	Set<Long> listProductIdByOrganizationId(@Param("orgId")Long orgId);


	@Query(value = "select distinct NEW com.nasnav.dto.response.navbox.ThreeSixtyProductsDTO(p.id, p.name, p.description)"+
            " from ProductEntity p join ProductVariantsEntity v on p = v.productEntity" +
            " join StocksEntity s on s.productVariantsEntity = v" +
            " where s.shopsEntity.id = :shopId and p.search360 = true " +
            " and (v.barcode like %:name% or p.barcode like %:name% " +
            " or LOWER(p.name) like %:name% or LOWER(p.description) like %:name%)")
    List<ThreeSixtyProductsDTO> find360Products(@Param("name") String name, @Param("shopId") Long shopId);


    @Modifying
    @Transactional
	@Query(value = "update Products set hide = :hide where organization_id = :orgId and id in :ids",
            nativeQuery = true)
    void setProductsHidden(@Param("ids") List<Long> ids,
                           @Param("hide") Boolean hide,
                           @Param("orgId") Long orgId);

	Long countByHideAndOrganizationId(boolean b, Long orgId);
	
	
	@Modifying
    @Transactional
	@Query(value = "DELETE FROM Product_tags t WHERE t.product_id in :productIds", nativeQuery = true)
	void deleteAllTagsForProducts(@Param("productIds")List<Long> products);
	
	
	@Modifying
    @Transactional
	@Query(value = "insert into product_tags (product_id, tag_id) values (:productId, :tagId)", nativeQuery = true)
	void insertProductTag(@Param("productId")Long productId, @Param("tagId")Long tagId);
	
	
	@Query("SELECT product.id from ProductEntity product where product.id in :ids")
	List<Long> getExistingProductIds(@Param("ids")List<Long> productIds);


    @Modifying
    @Transactional
    @Query(value = "update Products set search_360 = :include where organization_id = :orgId and id in :ids",
            nativeQuery = true)
    void includeProductsIn360Search(@Param("ids") List<Long> ids,
                           @Param("include") Boolean include,
                           @Param("orgId") Long orgId);
}



