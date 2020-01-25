package com.nasnav.dao;

import com.nasnav.persistence.ShopsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShopsRepository extends CrudRepository<ShopsEntity,Long> {

    List<ShopsEntity> findByOrganizationEntity_Id(Long organizationId);

    @Query(value = "select * from shops where id in (select shop_id from products where name like %:name%)" +
            " and long between :minLong and :maxLong and lat between :minLat and :maxLat and organization_id = :orgId", nativeQuery = true)
    List<ShopsEntity> getShopsByLocation(@Param("orgId") Long orgId,
                                         @Param("name") String name,
                                         @Param("minLong") Double minLong,
                                         @Param("maxLong") Double maxLong,
                                         @Param("minLat") Double minLat,
                                         @Param("maxLat") Double maxLat);
}
