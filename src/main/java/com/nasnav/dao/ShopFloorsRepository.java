package com.nasnav.dao;

import com.nasnav.persistence.ShopFloorsEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ShopFloorsRepository extends CrudRepository<ShopFloorsEntity, Long> {

    List<ShopFloorsEntity> findByShopThreeSixtyEntity_Id(Long shopId);

    void deleteByShopThreeSixtyEntity_IdAndOrganizationEntity_id(Long shopId, Long orgId);

}
