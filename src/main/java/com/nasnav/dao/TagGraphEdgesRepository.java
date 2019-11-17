package com.nasnav.dao;

import com.nasnav.persistence.TagGraphEdgesEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TagGraphEdgesRepository extends CrudRepository<TagGraphEdgesEntity, Long> {

    List<TagGraphEdgesEntity> findByOrganizationEntity_Id(Long orgId);
}
