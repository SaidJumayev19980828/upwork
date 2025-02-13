package com.nasnav.dao;

import com.nasnav.persistence.BundleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BundleRepository extends JpaRepository<BundleEntity, Long> {
    @Query("select min(item.quantity) FROM BundleEntity b JOIN b.items item where b.id = :id")
    Integer getStockQuantity(@Param("id") Long bundleId);

    @Query("select distinct prod.id FROM BundleEntity b  "
    		+ " JOIN b.items item "
    		+ " JOIN item.productVariantsEntity var "
    		+ " JOIN var.productEntity prod" +
            " where b.id = :id")
    List<Long> getBundleItemsProductIds(@Param("id") Long id);
    
    
    @Query("select b FROM BundleEntity b  "
    		+ " JOIN b.items item "
    		+ " JOIN item.productVariantsEntity var "
    		+ " JOIN var.productEntity prod" +
            " where prod.id in :ids")
    List<BundleEntity> getBundlesHavingItemsWithProductIds(@Param("ids") List<Long> itemProductIds);
    
    
    BundleEntity findFirstByOrderByNameDesc();

	Long countByOrganizationId(Long orgId);

    @Transactional
    @Modifying
    @Query( value = "update BundleEntity p set p.removed = 1 where p.id = :bundleId and p.productType = 1" )
    void deleteById(@Param("bundleId")Long bundleId);
}
