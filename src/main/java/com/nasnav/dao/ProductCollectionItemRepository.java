package com.nasnav.dao;

import com.nasnav.persistence.ProductCollectionItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

public interface ProductCollectionItemRepository extends JpaRepository<ProductCollectionItemEntity, Long> {

    Long countByItem_ProductEntity_IdIn(List<Long> productIds);
    Long countByItem_ProductEntity_OrganizationIdIn(Long orgId);

    @Transactional
    @Modifying
    @Query("delete from ProductCollectionItemEntity item where item in :items")
    void deleteItems(@Param("items") Set<ProductCollectionItemEntity> oldItems);

    @Transactional
    @Modifying
    @Query("delete from ProductCollectionItemEntity item where item.item.id in (select v.id from ProductVariantsEntity v where v.productEntity.id in :productIds) ")
    void deleteItemsByProductIds(@Param("productIds") List<Long> productIds);

    @Transactional
    @Modifying
    @Query("delete from ProductCollectionItemEntity item where item.item in (select v from ProductVariantsEntity v where v.productEntity.organizationId = :orgId) ")
    void deleteItemsByOrganizationId(@Param("orgId") Long orgId);
}
