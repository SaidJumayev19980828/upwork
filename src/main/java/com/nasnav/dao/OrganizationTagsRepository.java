package com.nasnav.dao;

import com.nasnav.persistence.OrganizationTagsEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrganizationTagsRepository extends CrudRepository<OrganizationTagsEntity, Long> {

    List<OrganizationTagsEntity> findByIdIn(List<Long> ids);
    List<OrganizationTagsEntity> findByOrganizationEntity_Id(Long orgId);
    OrganizationTagsEntity findByIdAndOrganizationEntity_Id(Long id, Long orgId);
    List<OrganizationTagsEntity> findByTagsEntity_IdIn(List<Long> tagsIds);
    List<OrganizationTagsEntity> findByTagsEntity_IdInAndOrganizationEntity_Id(List<Long> tagsIds, Long orgId);
    OrganizationTagsEntity findByTagsEntity_IdAndOrganizationEntity_Id(Long tagId, Long orgId);
}
