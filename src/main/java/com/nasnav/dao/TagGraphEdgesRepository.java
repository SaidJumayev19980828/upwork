package com.nasnav.dao;

import com.nasnav.dto.Pair;
import com.nasnav.persistence.TagGraphEdgesEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface TagGraphEdgesRepository extends CrudRepository<TagGraphEdgesEntity, Long> {

    TagGraphEdgesEntity findByParentIdAndChildId(Long parentId, Long childId);
    List<TagGraphEdgesEntity> findByParentIdNullAndChildIdIn(Set<Long> childrenIds);
    List<TagGraphEdgesEntity> findByChildIdIn(Set<Long> childrenIds);
    List<TagGraphEdgesEntity> findByParentIdIsNotNullAndChildId(Long childId);
    TagGraphEdgesEntity findByParentIdIsNullAndChildId(Long childId);

    
    @Query(value = "select edge from TagGraphEdgesEntity edge "
    		+ " left join edge.parent parentNode "
    		+ " left join parentNode.tag parentTag "
    		+ " left join edge.child childNode "
    		+ " left join childNode.tag childTag "
    		+ " where parentTag.id in :ids or childTag.id in :ids")
    List<TagGraphEdgesEntity> getTagsLinks(@Param("ids") List<Long> ids);

    
    @Query("select edge from TagGraphEdgesEntity edge "
    		+ " left join edge.parent parentNode "
    		+ " left join parentNode.tag parentTag "
    		+ " left join parentTag.organizationEntity parentTagOrg"
    		+ " left join edge.child childNode "
    		+ " left join childNode.tag childTag "
    		+ " left join childTag.organizationEntity childTagOrg"
    		+ " where parentTagOrg.id = :orgId or childTagOrg.id = :orgId")
    List<TagGraphEdgesEntity> findByOrganizationId(@Param("orgId") Long orgId);
}
