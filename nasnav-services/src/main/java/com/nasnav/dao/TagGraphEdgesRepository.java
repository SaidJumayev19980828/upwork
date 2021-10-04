package com.nasnav.dao;

import com.nasnav.persistence.TagGraphEdgesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TagGraphEdgesRepository extends JpaRepository<TagGraphEdgesEntity, Long> {

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
	@Query("select edge from TagGraphEdgesEntity edge ")
	List<TagGraphEdgesEntity> findAllTagGraph();

	@Query("select edge from TagGraphEdgesEntity edge "
			+ " left join edge.parent parentNode "
			+ " left join parentNode.tag parentTag "
			+ " left join parentTag.organizationEntity parentTagOrg"
			+ " left join edge.child childNode "
			+ " left join childNode.tag childTag "
			+ " left join childTag.organizationEntity childTagOrg"
			+ " where parentTagOrg.yeshteryState = :yeshteryState or childTagOrg.yeshteryState = :yeshteryState")
	List<TagGraphEdgesEntity> findAllYeshteryTagGraph(@Param("yeshteryState") int yeshteryState);

}
