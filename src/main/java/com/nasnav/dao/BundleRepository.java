package com.nasnav.dao;

import com.nasnav.persistence.BundleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BundleRepository extends JpaRepository<BundleEntity, Long> {
    @Query( value= 
    		"select coalesce(min(q.quantity), 0) \n" + 
    		"from (\n" + 
    		"	select coalesce(stock.quantity, 0) as quantity\n" + 
    		"	from public.product_bundles bundle\n" + 
    		"	inner join public.products prod\n" + 
    		"	on bundle.item_product_id = prod.id\n" + 
    		"	inner join public.stocks stock\n" + 
    		"	on prod.id = stock.product_id\n" + 
    		"	and stock.shop_id = :shopId\n" + 
    		"	where bundle.product_id = :id\n" + 
    		"	union all \n" + 
    		"	select coalesce(stock.quantity, 0) as quantity\n" + 
    		"	from public.product_bundles bundle\n" + 
    		"	inner join public.product_variants var\n" + 
    		"	on bundle.item_variant_id = var.id\n" + 
    		"	inner join public.stocks stock\n" + 
    		"	on var.id = stock.variant_id\n" + 
    		"	and stock.shop_id = :shopId\n" + 
    		"	where bundle.product_id = :id\n" + 
    		") q"
    		, nativeQuery = true)
    Integer getStockQuantity(@Param("id") Long bundleId, @Param("shopId") Long shopId);
    

    @Query("select distinct prod.id FROM BundleEntity b JOIN b.productItems prod" +
            " where b.id = :id")
    List<Long> GetBundleItemsProductIds(@Param("id") Long id);
    
    
    BundleEntity findFirstByOrderByNameDesc();

	Long countByOrganizationId(Long orgId);

	Long countByCategoryId(Long categoryId);

	BundleEntity findFirstByCategoryIdOrderByNameAsc(Long categoryId);
}
