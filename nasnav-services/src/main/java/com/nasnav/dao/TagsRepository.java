package com.nasnav.dao;

import com.nasnav.persistence.CategoriesEntity;
import com.nasnav.persistence.TagsEntity;
import com.nasnav.persistence.dto.query.result.products.ProductTagsBasicData;
import com.nasnav.service.model.IdAndNamePair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TagsRepository extends CrudRepository<TagsEntity, Long> {

    List<TagsEntity> findByIdIn(List<Long> ids);
    List<TagsEntity> findByOrganizationEntity_Id(Long orgId);
    Page<TagsEntity> findByOrganizationEntityIdOrderByPriorityDesc(Long orgId, Pageable pageable);
    Page<TagsEntity> findByOrganizationEntityIdInOrderByPriorityDesc(Set<Long> orgIds , Pageable pageable);
    Optional<TagsEntity> findByIdAndOrganizationEntity_Id(Long id, Long orgId);
    List<TagsEntity> findByCategoriesEntity_IdAndOrganizationEntity_Id(Long tagId, Long orgId);
    List<TagsEntity> findByIdInAndOrganizationEntity_Id(List<Long> ids, Long orgId);

    @Query(value = "select t from TagsEntity t " +
            " left join fetch t.categoriesEntity c" +
            " left join fetch t.organizationEntity o" +
            " where c.name = :categoryName and o.id = :orgId" +
            " order by t.priority desc",

            countQuery = "select count(t) from TagsEntity t " +
            " left join t.categoriesEntity c" +
            " left join t.organizationEntity o" +
            " where c.name = :categoryName and o.id = :orgId")
    Page<TagsEntity> findByCategoriesEntityNameAndOrganizationEntityIdOrderPriorityDesc(@Param("categoryName") String categoryName,
                                                                                        @Param("orgId") Long orgId,
                                                                                        Pageable pageable);

    Page<TagsEntity> findByCategoriesEntityNameAndOrganizationEntityIdInOrderByPriorityDesc(String categoryName, Set<Long> orgIds, Pageable pageable);

    @Query("SELECT tag FROM TagsEntity tag " +
            " LEFT JOIN tag.organizationEntity org " +
            " WHERE org.id = :orgId " +
            " AND lower(tag.name) in :tags ")
	Set<TagsEntity> findByNameLowerCaseInAndOrganizationEntity_Id(@Param("tags")Set<String> tags, @Param("orgId")Long orgId);

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

    @Transactional
    @Modifying
    @Query(value = "update TagsEntity t set t.categoriesEntity = :category where t.id in :ids")
    void setTagsListCategory(@Param("category") CategoriesEntity categoryId, @Param("ids") List<Long> ids);
    
    
    @Query("SELECT NEW com.nasnav.persistence.dto.query.result.products.ProductTagsBasicData(product.id , tags.id, tags.name) "
    		+ " from ProductEntity product "
    		+ " left join product.tags tags "
    		+ " where product.id in :productIds")
    List<ProductTagsBasicData> getTagsByProductIdIn(@Param("productIds")List<Long> productIds);


    @Query("select t from TagsEntity t where LOWER(t.name) like %:name% and t.organizationEntity.id = :orgId and t.categoriesEntity.name = 'COLLECTION' ")
    List<TagsEntity> findByNameAndOrganizationIdAndCategoryId(@Param("name") String name,
                                                              @Param("orgId") Long orgId);
    
    @Query("SELECT tag.id from TagsEntity tag where tag.id in :ids and tag.organizationEntity.id = :orgId")
    List<Long> getExistingTagIds(@Param("ids") Set<Long> tagIds, @Param("orgId") Long orgId);

    @Query("select t.id from TagsEntity t where t.categoriesEntity.id  = :id")
    List<Long> findByCategoryId(@Param("id") Long id);

    @Query(value = "select new com.nasnav.service.model.IdAndNamePair(t.id, t.pname) from TagsEntity t" +
            "  where t.organizationEntity.id = :orgId ")
    List<IdAndNamePair> getTagIdAndNamePairs(@Param("orgId") Long orgId);

    boolean existsByIdAndOrganizationEntity_Id(Long tagId, Long orgId);

    @Query("SELECT tag FROM TagsEntity tag " +
            " WHERE lower(tag.name) = :categoryName ")
    List<TagsEntity> findByCategoriesEntity_NameOrderByName(String categoryName);

}
