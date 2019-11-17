package com.nasnav.dao;

import com.nasnav.persistence.OrganizationTagsEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrganizationTagsRepository extends CrudRepository<OrganizationTagsEntity, Long> {

    List<OrganizationTagsEntity> findByOrganizationEntity_Id(Long orgId);
}
