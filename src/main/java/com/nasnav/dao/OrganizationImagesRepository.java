package com.nasnav.dao;

import com.nasnav.persistence.OrganizationImagesEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrganizationImagesRepository extends CrudRepository<OrganizationImagesEntity, Long> {

    List<OrganizationImagesEntity> findByOrganizationEntityIdAndTypeNotIn(Long id, List<Integer> types);
    List<OrganizationImagesEntity> findByShopsEntityIdAndTypeNot(Long id, Integer type);

    List<OrganizationImagesEntity> findByOrganizationEntityIdAndTypeOrderByIdDesc(Long id, Integer type);
}
