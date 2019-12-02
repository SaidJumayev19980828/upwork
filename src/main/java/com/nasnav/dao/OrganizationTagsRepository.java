package com.nasnav.dao;

import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.OrganizationTagsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrganizationTagsRepository extends CrudRepository<OrganizationTagsEntity, Long> {

    List<OrganizationTagsEntity> findByIdIn(List<Long> ids);
    List<OrganizationTagsEntity> findByOrganizationEntity_Id(Long orgId);
    OrganizationTagsEntity findByIdAndOrganizationEntity_Id(Long id, Long orgId);
    List<OrganizationTagsEntity> findByTagsEntity_IdIn(List<Long> tagsIds);
    List<OrganizationTagsEntity> findByTagsEntity_IdInAndOrganizationEntity_Id(List<Long> tagsIds, Long orgId);
    OrganizationTagsEntity findByTagsEntity_IdAndOrganizationEntity_Id(Long tagId, Long orgId);
    List<OrganizationTagsEntity> findByIdInAndOrganizationEntity_Id(List<Long> ids, Long orgId);

    @Query("select t from OrganizationTagsEntity t inner join TagGraphEdgesEntity g on g.childId = t.id where t.organizationEntity = :org")
    List<OrganizationTagsEntity> getTagsByOrgId(@Param("org") OrganizationEntity org);
}
