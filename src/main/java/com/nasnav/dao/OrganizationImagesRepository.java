package com.nasnav.dao;

import com.nasnav.persistence.OrganizationImagesEntity;
import org.springframework.data.repository.CrudRepository;

public interface OrganizationImagesRepository extends CrudRepository<OrganizationImagesEntity, Long> {

    //OrganizationImagesEntity getById(Long id);
}
