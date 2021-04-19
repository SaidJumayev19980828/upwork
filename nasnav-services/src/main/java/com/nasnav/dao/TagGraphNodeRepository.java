package com.nasnav.dao;

import com.nasnav.persistence.TagGraphNodeEntity;
import com.nasnav.service.model.IdAndNamePair;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

public interface TagGraphNodeRepository extends CrudRepository<TagGraphNodeEntity, Long>{

	@Query("select node from TagGraphNodeEntity node "
			+ " join fetch node.tag tag"
			+ " join fetch tag.organizationEntity org "
			+ " join fetch tag.categoriesEntity category "
			+ " where org.id = :orgId order by node.id" )
	List<TagGraphNodeEntity> findByTag_OrganizationEntity_Id(@Param("orgId")Long orgId);

	List<TagGraphNodeEntity> findByTag_Id(Long tagId);


	@Query(value = "select edge.parent_id as id from tag_graph_edges edge where edge.parent_id in" +
			" (select node.id from tag_graph_nodes node where node.tag_id in (select id from tags where organization_id = :orgId))" +
			" UNION select edge.child_id as id from tag_graph_edges edge where edge.parent_id in" +
			" (select node.id from tag_graph_nodes node where node.tag_id in (select id from tags where organization_id = :orgId))" ,
			nativeQuery = true)
	Set<BigInteger> findUsedTagsNodes(@Param("orgId")Long orgId);

	@Transactional
	@Modifying
	@Query(value = "delete from TagGraphNodeEntity node where node.id not in :usedNodesIds and node.tag in " +
			" (select tag from TagsEntity tag where tag.organizationEntity.id = :orgId)")
	void deleteByIdNotIn(@Param("usedNodesIds") Set<Long> usedNodesIds,
						 @Param("orgId") Long orgId);

	@Transactional
	@Modifying
	@Query(value = "delete from TagGraphNodeEntity node where node.tag in " +
			" (select tag from TagsEntity tag where tag.organizationEntity.id = :orgId)")
	void deleteByOrgId(@Param("orgId") Long orgId);

	@Query(value = "select new com.nasnav.service.model.IdAndNamePair(node.id, t.pname) from TagGraphNodeEntity node " +
			" inner join node.tag t" +
			" where t.organizationEntity.id = :orgId")
	List<IdAndNamePair> getTagNodeIdAndNamePairs(@Param("orgId") Long orgId);
}
