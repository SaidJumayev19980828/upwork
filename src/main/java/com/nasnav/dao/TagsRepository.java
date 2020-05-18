package com.nasnav.dao;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.TagsEntity;
import com.nasnav.persistence.dto.query.result.products.ProductTagsBasicData;

public interface TagsRepository extends CrudRepository<TagsEntity, Long> {

    List<TagsEntity> findByIdIn(List<Long> ids);
    List<TagsEntity> findByOrganizationEntity_Id(Long orgId);
    List<TagsEntity> findByOrganizationEntity_IdOrderByName(Long orgId);
    Optional<TagsEntity> findByIdAndOrganizationEntity_Id(Long id, Long orgId);
    List<TagsEntity> findByCategoriesEntity_IdIn(List<Long> tagsIds);
    List<TagsEntity> findByCategoriesEntity_IdInAndOrganizationEntity_Id(List<Long> tagsIds, Long orgId);
    List<TagsEntity> findByCategoriesEntity_IdAndOrganizationEntity_Id(Long tagId, Long orgId);
    List<TagsEntity> findByIdInAndOrganizationEntity_Id(List<Long> ids, Long orgId);

    List<TagsEntity> findByCategoriesEntity_NameAndOrganizationEntity_IdOrderByName(String categoryName, Long orgId);

    @Query("select t from TagsEntity t  where t.organizationEntity = :org and t.graphId is not null ")
    List<TagsEntity> getTagsByOrgId(@Param("org") OrganizationEntity org);
    
	Set<TagsEntity> findByNameInAndOrganizationEntity_Id(Set<String> tags, Long orgId);

	@Transactional
    @Modifying
    @Query(value = "update Tags set category_id = :categoryId where organization_id = :orgId and category_id is null",
    nativeQuery = true)
    void setAllTagsCategory(@Param("categoryId") Long categoryId, @Param("orgId") Long orgId);

    @Transactional
    @Modifying
    @Query(value = "update Tags set category_id = :categoryId where organization_id = :orgId and id in :ids",
            nativeQuery = true)
    void setTagsListCategory(@Param("categoryId") Long categoryId,
                             @Param("orgId") Long orgId,
                             @Param("ids") List<Long> ids);
    
    
    @Query("SELECT NEW com.nasnav.persistence.dto.query.result.products.ProductTagsBasicData(product.id , tags.id, tags.name) "
    		+ " from ProductEntity product "
    		+ " left join product.tags tags "
    		+ " where product.id in :productIds")
    List<ProductTagsBasicData> getTagsByProductIdIn(@Param("productIds")List<Long> productIds);


    @Query("select t from TagsEntity t where LOWER(t.name) like %:name% and t.organizationEntity.id = :orgId and t.categoriesEntity.id = :categoryId ")
    List<TagsEntity> findByNameAndOrganizationIdAndCategoryId(@Param("name") String name,
                                                              @Param("orgId") Long orgId,
                                                              @Param("categoryId") Long categoryId);
    
    @Query("SELECT tag.id from TagsEntity tag where tag.id in :ids")
    List<Long> getExistingTagIds(@Param("ids") List<Long> tagIds);
}
