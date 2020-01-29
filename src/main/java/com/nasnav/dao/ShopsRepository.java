package com.nasnav.dao;

import com.nasnav.persistence.ShopsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShopsRepository extends CrudRepository<ShopsEntity,Long> {

    List<ShopsEntity> findByOrganizationEntity_Id(Long organizationId);

    @Query(value = "select * from shops s where s.lng between :minLong and :maxLong and s.lat between :minLat and :maxLat and s.organization_id = :orgId" +
            " and s.id in (select st.shop_id from stocks st join Product_Variants v " +
            "on v.id = st.variant_id join Products p on p.id = v.product_id where p.name like %:name%)", nativeQuery = true)
    List<ShopsEntity> getShopsByLocation(@Param("orgId") Long orgId,
                                         @Param("name") String name,
                                         @Param("minLong") Double minLong,
                                         @Param("maxLong") Double maxLong,
                                         @Param("minLat") Double minLat,
                                         @Param("maxLat") Double maxLat);

    @Query(value = "select * from shops s where s.lng between :minLong and :maxLong and s.lat between :minLat and :maxLat " +
            " and s.id in (select st.shop_id from stocks st join Product_Variants v " +
            "on v.id = st.variant_id join Products p on p.id = v.product_id where p.name like %:name%)", nativeQuery = true)
    List<ShopsEntity> getShopsByLocation(@Param("name") String name,
                                         @Param("minLong") Double minLong,
                                         @Param("maxLong") Double maxLong,
                                         @Param("minLat") Double minLat,
                                         @Param("maxLat") Double maxLat);
}
