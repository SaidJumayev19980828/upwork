package com.nasnav.dao;

import com.nasnav.persistence.TagGraphEdgesEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TagGraphEdgesRepository extends CrudRepository<TagGraphEdgesEntity, Long> {

    List<TagGraphEdgesEntity> findByOrganizationEntity_IdOrderByIdAsc(Long orgId);
    List<TagGraphEdgesEntity> findByChildIdAndOrganizationEntity_Id(Long id, Long orgId);
    TagGraphEdgesEntity findByParentIdAndChildIdAndOrganizationEntity_Id(Long parentId, Long childId, Long orgId);
    TagGraphEdgesEntity findByParentIdNotNullAndChildIdAndOrganizationEntity_Id(Long childId, Long orgId);
    TagGraphEdgesEntity findByParentIdNullAndChildIdAndOrganizationEntity_Id(Long childId, Long orgId);
}
