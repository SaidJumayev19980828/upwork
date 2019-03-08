package com.nasnav.dao;

import java.util.List;

import com.nasnav.persistence.OrganizationEntity;
import org.springframework.data.repository.CrudRepository;

public interface OrganizationRepository extends CrudRepository<OrganizationEntity, Integer> {

    List<OrganizationEntity> findByName(String name);

    OrganizationEntity findOneByName(String name);

    OrganizationEntity findOneByNameContainingIgnoreCase(String name);

    OrganizationEntity findOneById(Long id);
}



