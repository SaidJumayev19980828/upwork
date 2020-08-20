package com.nasnav.dao;

import com.nasnav.persistence.OrganizationImagesEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrganizationImagesRepository extends CrudRepository<OrganizationImagesEntity, Long> {

    List<OrganizationImagesEntity> findByOrganizationEntityId(Long id);
    List<OrganizationImagesEntity> findByShopsEntityId(Long id);

    OrganizationImagesEntity findByOrganizationEntityIdAndType(Long id, Integer type);
}
