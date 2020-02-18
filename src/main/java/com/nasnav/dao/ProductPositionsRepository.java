package com.nasnav.dao;

import com.nasnav.persistence.ProductPositionEntity;
import org.springframework.data.repository.CrudRepository;

public interface ProductPositionsRepository extends CrudRepository<ProductPositionEntity, Long> {

    ProductPositionEntity findByShopsThreeSixtyEntity_Id(Long shopId);
}
