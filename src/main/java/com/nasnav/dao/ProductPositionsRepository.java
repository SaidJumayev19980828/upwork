package com.nasnav.dao;

import com.nasnav.persistence.ProductPositionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ProductPositionsRepository extends CrudRepository<ProductPositionEntity, Long> {

    ProductPositionEntity findByShopsThreeSixtyEntity_Id(Long shopId);

    @Transactional
    void deleteByShopsThreeSixtyEntity_Id(Long shopId);
}
