package com.nasnav.persistence;

import org.springframework.data.repository.CrudRepository;

public interface SocialRepository extends CrudRepository<SocialEntity, Integer> {

    SocialEntity findOneByOrganizationEntity_Id(Long OrganizationEntity_Id);
}
