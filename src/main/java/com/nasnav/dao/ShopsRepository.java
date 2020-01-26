package com.nasnav.dao;

import com.nasnav.persistence.ShopsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShopsRepository extends CrudRepository<ShopsEntity,Long> {

    List<ShopsEntity> findByOrganizationEntity_Id(Long organizationId);

    @Query(value = "select s from ShopsEntity s where s in (select p.shopsEntity from ProductsEntity p where p.name like %:name%)" +
            " and s.lng between :minLong and :maxLong and s.lat between :minLat and :maxLat and s.organizationEntity.id = :orgId")
    List<ShopsEntity> getShopsByLocation(@Param("orgId") Long orgId,
                                         @Param("name") String name,
                                         @Param("minLong") Double minLong,
                                         @Param("maxLong") Double maxLong,
                                         @Param("minLat") Double minLat,
                                         @Param("maxLat") Double maxLat);
}
