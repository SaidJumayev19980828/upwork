package com.nasnav.dao;

import com.nasnav.persistence.OrganizationThemeEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface OrganizationThemeRepository extends CrudRepository<OrganizationThemeEntity,Long> {

    Optional<OrganizationThemeEntity> findOneByOrganizationEntity_Id(Long OrganizationEntity_Id);
}
