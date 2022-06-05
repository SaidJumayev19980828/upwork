package com.nasnav.dao;

import com.nasnav.persistence.ShopSectionsEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ShopSectionsRepository extends CrudRepository<ShopSectionsEntity, Long> {

    Optional<ShopSectionsEntity> findByIdAndOrganizationEntity_Id(Long id, Long orgId);

    Optional<ShopSectionsEntity> findByIdAndShopFloorsEntity_Id(Long id, Long floorId);
}
