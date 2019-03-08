package com.nasnav.dao;

import com.nasnav.persistence.OrganizationThemeEntity;
import org.springframework.data.repository.CrudRepository;

public interface OrganizationThemeRepository extends CrudRepository<OrganizationThemeEntity,Long> {

    OrganizationThemeEntity findOneByOrganizationEntity_Id(Long OrganizationEntity_Id);
}
