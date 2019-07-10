package com.nasnav.dao;

import com.nasnav.persistence.ExtraAttributesEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ExtraAttributesRepository extends CrudRepository<ExtraAttributesEntity, Integer> {

    List<ExtraAttributesEntity> findAll();
    List<ExtraAttributesEntity> findByOrganizationId(Long organizationId);
}
