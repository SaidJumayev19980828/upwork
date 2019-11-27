package com.nasnav.dao;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.nasnav.dto.Pair;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.nasnav.persistence.ProductEntity;

public interface ProductRepository extends CrudRepository<ProductEntity,Long> {

    List<ProductEntity> findByOrganizationId(Long organizationId);
    List<ProductEntity> findByOrganizationIdOrderByIdAsc(Long organizationId);
    List<ProductEntity> findByOrganizationIdOrderByIdDesc(Long organizationId);

    List<ProductEntity> findByOrganizationIdOrderByNameAsc(Long organizationId);
    List<ProductEntity> findByOrganizationIdOrderByNameDesc(Long organizationId);

    List<ProductEntity> findByOrganizationIdOrderByPnameAsc(Long organizationId);
    List<ProductEntity> findByOrganizationIdOrderByPnameDesc(Long organizationId);

    List<ProductEntity> findByOrganizationIdAndCategoryId(Long organizationId, Long categoryId);
    List<ProductEntity> findByOrganizationIdAndCategoryIdOrderByIdAsc(Long organizationId,Long categoryId);
    List<ProductEntity> findByOrganizationIdAndCategoryIdOrderByIdDesc(Long organizationId,Long categoryId);
    List<ProductEntity> findByOrganizationIdAndCategoryIdOrderByNameAsc(Long organizationId,Long categoryId);
    List<ProductEntity> findByOrganizationIdAndCategoryIdOrderByNameDesc(Long organizationId,Long categoryId);
    List<ProductEntity> findByOrganizationIdAndCategoryIdOrderByPnameAsc(Long organizationId,Long categoryId);
    List<ProductEntity> findByOrganizationIdAndCategoryIdOrderByPnameDesc(Long organizationId,Long categoryId);

    List<ProductEntity> findByOrganizationIdAndBrandId(Long organizationId, Long brandId);
    List<ProductEntity> findByOrganizationIdAndBrandIdOrderByIdAsc(Long organizationId,Long brandId);
    List<ProductEntity> findByOrganizationIdAndBrandIdOrderByIdDesc(Long organizationId,Long brandId);
    List<ProductEntity> findByOrganizationIdAndBrandIdOrderByNameAsc(Long organizationId,Long brandId);
    List<ProductEntity> findByOrganizationIdAndBrandIdOrderByNameDesc(Long organizationId,Long brandId);
    List<ProductEntity> findByOrganizationIdAndBrandIdOrderByPnameAsc(Long organizationId,Long brandId);
    List<ProductEntity> findByOrganizationIdAndBrandIdOrderByPnameDesc(Long organizationId,Long brandId);

    List<ProductEntity> findByOrganizationIdAndCategoryIdAndBrandId(Long organizationId, Long categoryId, Long brandId);
    List<ProductEntity> findByOrganizationIdAndCategoryIdAndBrandIdOrderByIdAsc(Long organizationId,Long categoryId,
                                                                                Long brandId);
    List<ProductEntity> findByOrganizationIdAndCategoryIdAndBrandIdOrderByIdDesc(Long organizationId,Long categoryId,
                                                                                 Long brandId);
    List<ProductEntity> findByOrganizationIdAndCategoryIdAndBrandIdOrderByNameAsc(Long organizationId,Long categoryId,
                                                                                  Long brandId);
    List<ProductEntity> findByOrganizationIdAndCategoryIdAndBrandIdOrderByNameDesc(Long organizationId,Long categoryId,
                                                                                   Long brandId);
    List<ProductEntity> findByOrganizationIdAndCategoryIdAndBrandIdOrderByPnameAsc(Long organizationId,Long categoryId,
                                                                                   Long brandId);
    List<ProductEntity> findByOrganizationIdAndCategoryIdAndBrandIdOrderByPnameDesc(Long organizationId,Long categoryId,
                                                                                    Long brandId);

    List<ProductEntity> findByIdIn(List<Long> ids);
    List<ProductEntity> findByIdInOrderByIdAsc(List<Long> ids);
    List<ProductEntity> findByIdInOrderByIdDesc(List<Long> ids);
    List<ProductEntity> findByIdInOrderByNameAsc(List<Long> ids);
    List<ProductEntity> findByIdInOrderByNameDesc(List<Long> ids);
    List<ProductEntity> findByIdInOrderByPnameAsc(List<Long> ids);
    List<ProductEntity> findByIdInOrderByPnameDesc(List<Long> ids);

//    List<ProductEntity> findByIdInOrderByPriceAsc(List<Long> ids);
//    List<ProductEntity> findByIdInOrderByPriceDesc(List<Long> ids);

    List<ProductEntity> findByIdInAndCategoryId(List<Long> ids, Long categoryId);
    List<ProductEntity> findByIdInAndCategoryIdOrderByIdAsc(List<Long> ids, Long categoryId);
    List<ProductEntity> findByIdInAndCategoryIdOrderByIdDesc(List<Long> ids, Long categoryId);
    List<ProductEntity> findByIdInAndCategoryIdOrderByNameAsc(List<Long> ids, Long categoryId);
    List<ProductEntity> findByIdInAndCategoryIdOrderByNameDesc(List<Long> ids, Long categoryId);
    List<ProductEntity> findByIdInAndCategoryIdOrderByPnameAsc(List<Long> ids, Long categoryId);
    List<ProductEntity> findByIdInAndCategoryIdOrderByPnameDesc(List<Long> ids, Long categoryId);

    List<ProductEntity> findByIdInAndBrandId(List<Long> ids, Long brandId);
    List<ProductEntity> findByIdInAndBrandIdOrderByIdAsc(List<Long> ids, Long brandId);
    List<ProductEntity> findByIdInAndBrandIdOrderByIdDesc(List<Long> ids, Long brandId);
    List<ProductEntity> findByIdInAndBrandIdOrderByNameAsc(List<Long> ids, Long brandId);
    List<ProductEntity> findByIdInAndBrandIdOrderByNameDesc(List<Long> ids, Long brandId);
    List<ProductEntity> findByIdInAndBrandIdOrderByPnameAsc(List<Long> ids, Long brandId);
    List<ProductEntity> findByIdInAndBrandIdOrderByPnameDesc(List<Long> ids, Long brandId);

    List<ProductEntity> findByIdInAndCategoryIdAndBrandId(List<Long> ids, Long categoryId, Long brandId);
    List<ProductEntity> findByIdInAndCategoryIdAndBrandIdOrderByIdAsc(List<Long> ids, Long categoryId, Long brandId);
    List<ProductEntity> findByIdInAndCategoryIdAndBrandIdOrderByIdDesc(List<Long> ids, Long categoryId, Long brandId);
    List<ProductEntity> findByIdInAndCategoryIdAndBrandIdOrderByNameAsc(List<Long> ids, Long categoryId, Long brandId);
    List<ProductEntity> findByIdInAndCategoryIdAndBrandIdOrderByNameDesc(List<Long> ids, Long categoryId, Long brandId);
    List<ProductEntity> findByIdInAndCategoryIdAndBrandIdOrderByPnameAsc(List<Long> ids, Long categoryId, Long brandId);
    List<ProductEntity> findByIdInAndCategoryIdAndBrandIdOrderByPnameDesc(List<Long> ids, Long categoryId,
                                                                          Long brandId);
//    List<ProductEntity> findByIdInAndCategoryIdOrderByPriceAsc(List<Long> ids,Long categoryId);
//    List<ProductEntity> findByIdInAndCategoryIdOrderByPriceDesc(List<Long> ids,Long categoryId);

    @Query("SELECT distinct products.categoryId FROM ProductEntity products WHERE products.organizationId = :organizationId AND products.categoryId IS NOT NULL")
    List<Long> getOrganizationCategoriesId(@Param("organizationId") Long organizationId);

    @Query("SELECT products.id FROM ProductEntity products WHERE products.categoryId = :categoryId")
    List<Long> getProductsByCategoryId(@Param("categoryId") Long categoryId);
	
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
}



