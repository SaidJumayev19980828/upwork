package com.nasnav.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.nasnav.persistence.ExtraAttributesEntity;

public interface ExtraAttributesRepository extends CrudRepository<ExtraAttributesEntity, Integer> {

    List<ExtraAttributesEntity> findAll();
    List<ExtraAttributesEntity> findByOrganizationId(Long organizationId);
    Optional<ExtraAttributesEntity> findByNameAndOrganizationId(String name, Long organizationId);
}
