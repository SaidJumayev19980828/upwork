package com.nasnav.dao;

import com.nasnav.persistence.ShopFloorsEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ShopFloorsRepository extends CrudRepository<ShopFloorsEntity, Long> {

    List<ShopFloorsEntity> findByShopThreeSixtyEntity_IdOrderById(Long shopId);

    @Transactional
    void deleteByShopThreeSixtyEntity_IdAndOrganizationEntity_id(Long shopId, Long orgId);

    ShopFloorsEntity findByIdAndShopThreeSixtyEntity_Id(Long id, Long shopId);

}
