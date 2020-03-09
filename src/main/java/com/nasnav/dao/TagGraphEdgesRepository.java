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
    @Query(nativeQuery = true)
    List<Pair> getTagsLinks(@Param("childIds") Set<Long> childIds);

    @Query(value = "select t from TagGraphEdgesEntity t where t.parentId in :ids or t.childId in :ids")
    List<TagGraphEdgesEntity> getTagsLinks(@Param("ids") List<Long> ids);

    @Query(value = "select * from tag_graph_edges e where e.parent_id in (select t.id from tags t where organization_id = :orgId)" +
            " or e.child_id in (select t.id from Tags t where organization_id = :orgId)", nativeQuery = true)
    List<TagGraphEdgesEntity> findByOrganizationId(@Param("orgId") Long orgId);
}
