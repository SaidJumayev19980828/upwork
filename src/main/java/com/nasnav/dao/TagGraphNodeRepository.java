package com.nasnav.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.nasnav.persistence.TagGraphEdgesEntity;
import com.nasnav.persistence.TagGraphNodeEntity;

public interface TagGraphNodeRepository extends CrudRepository<TagGraphNodeEntity, Long>{

	@Query("select node from TagGraphNodeEntity node "
			+ " join fetch node.tag tag"
			+ " join fetch tag.organizationEntity org "
			+ " join fetch tag.categoriesEntity category "
			+ " where org.id = :orgId" )
	List<TagGraphNodeEntity> findByTag_OrganizationEntity_Id(@Param("orgId")Long orgId);

	List<TagGraphNodeEntity> findByTag_Id(Long tagId);
}
