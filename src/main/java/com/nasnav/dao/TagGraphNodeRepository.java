package com.nasnav.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.nasnav.persistence.TagGraphNodeEntity;

public interface TagGraphNodeRepository extends CrudRepository<TagGraphNodeEntity, Long>{

	List<TagGraphNodeEntity> findByTag_OrganizationEntity_Id(Long orgId);
}
