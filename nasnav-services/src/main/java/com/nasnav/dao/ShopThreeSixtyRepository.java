package com.nasnav.dao;

import com.nasnav.persistence.ShopThreeSixtyEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShopThreeSixtyRepository extends CrudRepository<ShopThreeSixtyEntity,Long> {

    Optional<ShopThreeSixtyEntity> findByIdAndShopsEntity_OrganizationEntity_Id(Long id, Long orgId);

    Optional<ShopThreeSixtyEntity> findByShopsEntity_IdAndShopsEntity_OrganizationEntity_Id(Long shopId, Long orgId);

    Optional<ShopThreeSixtyEntity> getFirstByShopsEntity_Id(Long shopId);

    @Query("select s from ShopThreeSixtyEntity s " +
            " left join fetch s.shopsEntity shop " +
            " left join fetch shop.organizationEntity o" +
            " where s.id = :shopId and o.yeshteryState = 1")
    Optional<ShopThreeSixtyEntity> getYeshteryShop(@Param("shopId") Long shopId);

    boolean existsByShopsEntity_Id(Long shopId);
}
