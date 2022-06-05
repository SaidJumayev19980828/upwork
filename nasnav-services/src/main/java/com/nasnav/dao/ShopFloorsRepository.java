package com.nasnav.dao;

import com.nasnav.persistence.ShopFloorsEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ShopFloorsRepository extends CrudRepository<ShopFloorsEntity, Long> {

    List<ShopFloorsEntity> findByShopThreeSixtyEntity_Id(Long shopId);

    Long countByShopThreeSixtyEntity_Id(Long shopId);
    @Transactional
    void deleteByShopThreeSixtyEntity_IdAndOrganizationEntity_id(Long shopId, Long orgId);

    Optional<ShopFloorsEntity> findByIdAndShopThreeSixtyEntity_Id(Long id, Long shopId);
}
