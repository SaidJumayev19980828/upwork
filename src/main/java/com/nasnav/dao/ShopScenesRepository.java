package com.nasnav.dao;

import com.nasnav.persistence.ShopScenesEntity;
import org.springframework.data.repository.CrudRepository;

public interface ShopScenesRepository extends CrudRepository<ShopScenesEntity, Long> {

    ShopScenesEntity findByIdAndOrganizationEntity_Id(Long id, Long orgId);
}
