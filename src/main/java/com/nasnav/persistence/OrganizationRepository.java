package com.nasnav.persistence;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface OrganizationRepository extends CrudRepository<OrganizationEntity, Integer> {

    List<OrganizationEntity> findByName(String name);
}



