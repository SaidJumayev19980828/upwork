package com.nasnav.dao;

import com.nasnav.persistence.ShopThreeSixtyEntity;
import org.springframework.data.repository.CrudRepository;

public interface ShopThreeSixtyRepository extends CrudRepository<ShopThreeSixtyEntity,Long> {

    ShopThreeSixtyEntity findByShopsEntity_Id(Long shopId);

}
