package com.nasnav.dao;

import com.nasnav.persistence.ShopSectionsEntity;
import org.springframework.data.repository.CrudRepository;

public interface ShopSectionsRepository extends CrudRepository<ShopSectionsEntity, Long> {

    ShopSectionsEntity findByIdAndOrganizationEntity_Id(Long id, Long orgId);
}
