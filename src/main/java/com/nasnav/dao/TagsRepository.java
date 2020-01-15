package com.nasnav.dao;

import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.TagsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TagsRepository extends CrudRepository<TagsEntity, Long> {

    List<TagsEntity> findByIdIn(List<Long> ids);
    List<TagsEntity> findByOrganizationEntity_Id(Long orgId);
    TagsEntity findByIdAndOrganizationEntity_Id(Long id, Long orgId);
    List<TagsEntity> findByCategoriesEntity_IdIn(List<Long> tagsIds);
    List<TagsEntity> findByCategoriesEntity_IdInAndOrganizationEntity_Id(List<Long> tagsIds, Long orgId);
    TagsEntity findByCategoriesEntity_IdAndOrganizationEntity_Id(Long tagId, Long orgId);
    List<TagsEntity> findByIdInAndOrganizationEntity_Id(List<Long> ids, Long orgId);

    @Query("select t from TagsEntity t inner join TagGraphEdgesEntity g on g.childId = t.id where t.organizationEntity = :org")
    List<TagsEntity> getTagsByOrgId(@Param("org") OrganizationEntity org);
}
