package com.nasnav.dao;

import com.nasnav.persistence.SocialEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SocialRepository extends CrudRepository<SocialEntity, Long> {

    Optional<SocialEntity> findOneByOrganizationEntity_Id(Long OrganizationEntity_Id);
}
