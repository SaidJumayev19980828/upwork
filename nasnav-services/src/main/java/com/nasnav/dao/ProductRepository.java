package com.nasnav.dao;

import com.nasnav.dto.Pair;
import com.nasnav.dto.ProductAddonsDTO;
import com.nasnav.dto.response.navbox.ThreeSixtyProductsDTO;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.service.model.IdAndNamePair;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductRepository extends CrudRepository<ProductEntity,Long> {

    List<ProductEntity> findByOrganizationId(Long organizationId);

    @Query("select p from ProductEntity p left join p.productVariants v " +
            "where p.removed = 0 and v.removed = 0 and p.organizationId = :orgId and p.productType = :type")
    List<ProductEntity> findEmptyProductsByOrganizationIdAndProductType(@Param("orgId") Long orgId,
                                                           @Param("type") Integer type);

    List<ProductEntity> findByIdIn(List<Long> ids);
    List<ProductEntity> findByIdInOrderByIdAsc(List<Long> ids);
    List<ProductEntity> findByIdInOrderByIdDesc(List<Long> ids);
    List<ProductEntity> findByIdInOrderByNameAsc(List<Long> ids);
    List<ProductEntity> findByIdInOrderByNameDesc(List<Long> ids);
    List<ProductEntity> findByIdInOrderByPnameAsc(List<Long> ids);
    List<ProductEntity> findByIdInOrderByPnameDesc(List<Long> ids);

    List<ProductEntity> findByIdInAndOrganizationId(List<Long> ids, Long orgId);

	Optional<ProductEntity> findByIdAndOrganizationId(Long id, Long orgId);

	Optional<ProductEntity> findByBarcodeAndOrganizationId(String barcode, Long orgId);
	Optional<ProductEntity> findByName(String name);

	@Query(value = "select p.id from Products p where p.category_id = :categoryId", nativeQuery = true)
    List<Long> findProductsIdsByCategoryId(@Param("categoryId") Long categoryId);

	@Query("SELECT p.id from ProductEntity p where p.organizationId = :orgId")
    List<Long> findProductsIdsByOrganizationId(@Param("orgId") Long orgId);

    @Query("select p from ProductEntity p " +
            " LEFT JOIN OrganizationEntity org on p.organizationId = org.id " +
            " where p.id = :id and p.productType in (0,1) " +
            " and ( (org.yeshteryState in (1)) OR :allowAll = true )")
    Optional<ProductEntity> findByProductId(@Param("id") Long id, @Param("allowAll") Boolean allowAll);

    @Query("select distinct p.id from ProductEntity p " +
            " LEFT JOIN OrganizationEntity org on p.organizationId = org.id " +
            " where p.brand.id = :id and p.removed = 0")
    List<Long> findByBrandId(@Param("id") Long id);

    @Query("select distinct p.id from ProductEntity p " +
            " where p.brand.id in :brandList and p.removed = 0")

    Set<Long> findByBrandIds(@Param("brandList") Set<Long> brandList);


	@Query("SELECT products FROM ProductEntity products "
			+ " LEFT JOIN FETCH products.productVariants variants "
			+ " LEFT JOIN FETCH products.tags tags "
			+ " LEFT JOIN FETCH variants.stocks stocks"
			+ " LEFT JOIN FETCH stocks.organizationEntity"
			+ " WHERE products.id in :productIdList")
	Set<ProductEntity> findFullDataByIdIn(@Param("productIdList") List<Long> productIdList);

	@Query("Select product from ProductEntity product left join fetch product.brand brand " +
            " where product.id = :id")
    ProductEntity getById(@Param("id") Long id);

    @Query(value = "SELECT Distinct t.product_id FROM Product_tags t WHERE t.tag_id in :tagsIds", nativeQuery = true)
    Set<Long> getProductIdsByTagsList(@Param("tagsIds") Set<Long> tagsIds);

    Long countByBrandId(Long brandId);


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
    

    @Query(value = "update Products set category_id = :categoryId where id in :productsIds", nativeQuery = true)
    @Transactional
    @Modifying
    void setProductsListCategory(@Param("categoryId") Long categoryId,
                                 @Param("productsIds") List<Long> productsIds);

	List<ProductEntity> findByNameAndOrganizationId(String name, Long orgId);
	
	long countByOrganizationId(long l);
    long countByOrganizationIdAndProductType(long l, Integer productType);
	
	

	@Transactional
    @Modifying
    @Query( value = "update public.products set removed = 1 where organization_Id = :orgId", nativeQuery = true )
	void deleteAllByOrganizationId(@Param("orgId")Long orgId);
	
	
	@Transactional
    @Modifying
    @Query( value = "update ProductEntity p set p.removed = 1 where p.id in :idList and p.productType = 0" )
	void deleteAllByIdIn(@Param("idList") List<Long> idList);
	
	
	@Query("select products.id FROM ProductEntity products where products.organizationId = :orgId and products.productType = 0")
	Set<Long> listProductIdByOrganizationId(@Param("orgId")Long orgId);


    @Query(value = "select distinct NEW com.nasnav.dto.response.navbox.ThreeSixtyProductsDTO(p.id, p.name, p.description, p.productType)"+
            " from ProductEntity p join ProductVariantsEntity v on v.productEntity = p" +
            " join v.stocks s "+
            " left join Shop360ProductsEntity sp on sp.shopEntity = s.shopsEntity and sp.productEntity = p " +
            " where s.shopsEntity.id = :shopId and p.productType = 0" +
            " and (:has360 = false OR (sp is not null and sp.published in (:published)))" +
            " and (v.barcode like %:name% or p.barcode like %:name% " +
            " or LOWER(p.name) like %:name% or LOWER(p.description) like %:name%" +
            " or LOWER(v.sku) like %:name% or LOWER(v.productCode) like %:name%)")
    List<ThreeSixtyProductsDTO> find360Products(@Param("name") String name,
                                                @Param("shopId") Long shopId,
                                                @Param("has360") boolean has360,
                                                @Param("published") List<Short> published,
                                                Pageable pageable);


    @Query(value = "select distinct NEW com.nasnav.dto.response.navbox.ThreeSixtyProductsDTO(p.id, p.name, p.description, p.productType)"+
            " from ProductCollectionEntity p " +
            " left join p.items item " +
            " join item.item v " +
            " join v.stocks s "+
            " left join Shop360ProductsEntity sp on sp.shopEntity = s.shopsEntity and sp.productEntity = p"+
            " where s.shopsEntity.id = :shopId and p.productType = 2" +
            " and (:has360 = false OR (sp is not null and sp.published in (:published)))" +
            " and (v.barcode like %:name% or p.barcode like %:name% " +
            " or LOWER(p.name) like %:name% or LOWER(p.description) like %:name%" +
            " or LOWER(v.sku) like %:name% or LOWER(v.productCode) like %:name%)")
    List<ThreeSixtyProductsDTO> find360Collections(@Param("name") String name,
                                                   @Param("shopId") Long shopId,
                                                   @Param("has360") boolean has360,
                                                   @Param("published") List<Short> published,
                                                   Pageable pageable);

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
	
	
	@Query("SELECT product.id from ProductEntity product where product.id in :ids and product.organizationId = :orgId")
	List<Long> getExistingProductIds(@Param("ids")Set<Long> productIds, @Param("orgId")Long orgId);

    @Query("SELECT product from ProductEntity product where product.id in :ids and product.organizationId = :orgId")
    List<ProductEntity> getExistingProducts(@Param("ids")Set<Long> productIds, @Param("orgId")Long orgId);

    @Query(value = "select distinct new com.nasnav.service.model.IdAndNamePair(p.id, p.pname) from ProductVariantsEntity v left join v.productEntity p " +
            "  where p.organizationId = :orgId and p.removed = 0 and p.productType = 0 and v.removed = 0")
    List<IdAndNamePair> getProductIdAndNamePairs(@Param("orgId") Long orgId);

    @Query(value = "select new com.nasnav.service.model.IdAndNamePair(p.id, p.pname) from ProductCollectionItemEntity item left join item.collection p " +
            "  where p.organizationId = :orgId and p.removed = 0 and item.item.removed = 0")
    List<IdAndNamePair> getCollectionIdAndNamePairs(@Param("orgId") Long orgId);

    long countByProductType(Integer productType);

    boolean existsByIdAndOrganizationId(Long productId, Long orgId);

    @Query(value = "select p from ProductEntity p " +
                    "left join p.brand b " +
                    "left join p.tags t " +
                    "where p.id in :productIds or b.id in :brandIds or t.id in :tagIds")
    List<ProductEntity> findProductsByProductIdsOrBrandIdsOrTagIds(@Param("productIds") Set<Long> productId,
                    @Param("brandIds") Set<Long> brandIds,
                    @Param("tagIds") Set<Long> tagId);
    

    @Query(nativeQuery = true)
    List<Pair> getProductAddons(@Param("productsIds") List<Long> productsIds, @Param("addonsIds") List<Long> addonsIds);
    
    @Query(value = "delete from Product_addons where addon_id = :addon_id", nativeQuery = true)
    @Transactional
    @Modifying
    void detachProductsFromAddon(@Param("addon_id") Long addonId);


}
