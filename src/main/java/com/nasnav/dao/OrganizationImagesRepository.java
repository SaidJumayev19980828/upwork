package com.nasnav.dao;

import com.nasnav.persistence.OrganizationImagesEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrganizationImagesRepository extends CrudRepository<OrganizationImagesEntity, Long> {

    List<OrganizationImagesEntity> findByOrganizationEntityIdAndShopsEntityNullAndTypeNotIn(Long id, List<Integer> types);
    List<OrganizationImagesEntity> findByShopsEntityIdAndTypeNot(Long id, Integer type);

    List<OrganizationImagesEntity> findByOrganizationEntityIdAndShopsEntityNullAndTypeOrderByIdDesc(Long id, Integer type);
}
