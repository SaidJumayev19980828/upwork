package com.nasnav.dao;

import com.nasnav.persistence.ShopsEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ShopsRepository extends CrudRepository<ShopsEntity,Long> {

    Optional<ShopsEntity> findByIdAndRemoved(Long id, Integer removed);

    List<ShopsEntity> findByOrganizationEntity_IdAndRemoved(Long organizationId, Integer removed);
    
    List<ShopsEntity> findByOrganizationEntity_IdAndRemovedAndIsWarehouse(Long organizationId, Integer removed, Integer isWarehouse);

    @Query(value = "select * from shops s where s.lng between :minLong and :maxLong and s.lat between :minLat and :maxLat and s.organization_id = :orgId" +
            " and s.id in (select st.shop_id from stocks st join Product_Variants v " +
            "on v.id = st.variant_id join Products p on p.id = v.product_id where p.name like %:name%) and s.removed = 0", nativeQuery = true)
    List<ShopsEntity> getShopsByLocation(@Param("orgId") Long orgId,
                                         @Param("name") String name,
                                         @Param("minLong") Double minLong,
                                         @Param("maxLong") Double maxLong,
                                         @Param("minLat") Double minLat,
                                         @Param("maxLat") Double maxLat);

    @Query(value = "select s from StocksEntity st" +
            " left join st.shopsEntity s" +
            " left join st.productVariantsEntity v " +
            " left join v.productEntity p " +
            " left join p.tags t " +
            "where (p.name like %:name% or t.name like %:name% ) and s.removed = 0")
    Set<ShopsEntity> getShopsByLocation(@Param("name") String name);

	Optional<ShopsEntity> findByNameAndOrganizationEntity_IdAndRemoved(String shopName, Long orgId, Integer removed);

	ShopsEntity findByIdAndOrganizationEntity_IdAndRemoved(Long id, Long orgId, Integer removed);

	List<ShopsEntity> findByIdInAndRemoved(Set<Long> shopIdList, Integer removed);

    Boolean existsByIdAndOrganizationEntity_IdAndRemoved(Long id, Long orgId, Integer removed);

    @Transactional
    @Modifying
    @Query(value = "update ShopsEntity s set s.removed = 1 where s.id = :id")
    void setShopHidden(@Param("id") Long id);


    @Query("SELECT shop from ShopsEntity shop where shop.id in :ids and shop.organizationEntity.id = :orgId")
    List<ShopsEntity> getExistingShops(@Param("ids")Set<Long> shopIds, @Param("orgId")Long orgId);

	List<ShopsEntity> findByIdInAndOrganizationEntity_IdAndRemoved(Set<Long> shops, Long orgId, int removed);
	
	@Query("SELECT shop FROM ShopsEntity shop "
			+ " LEFT JOIN FETCH shop.addressesEntity address "
			+ " LEFT JOIN FETCH address.areasEntity area "
			+ " LEFT JOIN FETCH area.citiesEntity city "
			+ " LEFT JOIN FETCH city.countriesEntity country "
			+ " WHERE shop.id = :id")
	Optional<ShopsEntity> findShopFullData(@Param("id")Long id);
}
