package com.nasnav.dao;

import com.nasnav.persistence.BundleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BundleRepository extends JpaRepository<BundleEntity, Long> {
    @Query("select min(item.quantity) FROM BundleEntity b JOIN b.items item where b.id = :id")
    Integer getStockQuantity(@Param("id") Long bundleId);

    @Query("select distinct prod.id FROM BundleEntity b JOIN b.items item JOIN item.productEntity prod" +
            " where b.id = :id")
    List<Long> GetBundleItemsProductIds(@Param("id") Long id);
    
    
    BundleEntity findFirstByOrderByNameDesc();

	Long countByOrganizationId(Long orgId);

	Long countByCategoryId(Long categoryId);

	BundleEntity findFirstByCategoryIdOrderByNameAsc(Long categoryId);
}
