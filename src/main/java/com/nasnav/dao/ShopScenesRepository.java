package com.nasnav.dao;

import com.nasnav.persistence.ShopScenesEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ShopScenesRepository extends CrudRepository<ShopScenesEntity, Long> {

    Optional<ShopScenesEntity> findByIdAndOrganizationEntity_Id(Long id, Long orgId);
}
