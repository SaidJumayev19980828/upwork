package com.nasnav.dao;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.TagsEntity;

public interface TagsRepository extends CrudRepository<TagsEntity, Long> {

    List<TagsEntity> findByIdIn(List<Long> ids);
    List<TagsEntity> findByOrganizationEntity_Id(Long orgId);
    TagsEntity findByIdAndOrganizationEntity_Id(Long id, Long orgId);
    List<TagsEntity> findByCategoriesEntity_IdIn(List<Long> tagsIds);
    List<TagsEntity> findByCategoriesEntity_IdInAndOrganizationEntity_Id(List<Long> tagsIds, Long orgId);
    List<TagsEntity> findByCategoriesEntity_IdAndOrganizationEntity_Id(Long tagId, Long orgId);
    List<TagsEntity> findByIdInAndOrganizationEntity_Id(List<Long> ids, Long orgId);

    List<TagsEntity> findByCategoriesEntity_NameAndOrganizationEntity_Id(String categoryName, Long orgId);

    @Query("select t from TagsEntity t  where t.organizationEntity = :org " +
            "and (t.id in(select l1.childId from TagGraphEdgesEntity l1) or t.id in(select l2.parentId from TagGraphEdgesEntity l2))")
    List<TagsEntity> getTagsByOrgId(@Param("org") OrganizationEntity org);
    
	Set<TagsEntity> findByNameInAndOrganizationEntity_Id(Set<String> tags, Long orgId);

}
