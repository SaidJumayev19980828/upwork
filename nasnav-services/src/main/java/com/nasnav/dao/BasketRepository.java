package com.nasnav.dao;

import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.dto.query.result.StockBasicData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface BasketRepository extends JpaRepository<BasketsEntity, Long> {

    @Query("select count(e) from BasketsEntity e "
    		+ " join e.ordersEntity o"
    		+ " join e.stocksEntity s "
    		+ " join s.productVariantsEntity v "
    		+ " join v.productEntity p "
    		+ " where p.id= :productId "
    		+ " and o.status = :status ")
	Long countByProductIdAndOrderEntity_status(Long productId, Integer status);
    
    @Query("SELECT NEW com.nasnav.persistence.dto.query.result.StockBasicData(variant.id , shop.id , stock.id) "
    		+ " from BasketsEntity basket "
    		+ " left join basket.stocksEntity stock "
    		+ " left join stock.shopsEntity shop "
    		+ " left join stock.productVariantsEntity variant"
    		+ " WHERE basket.id = :id")
    StockBasicData getItemStockBasicDataById( @Param("id") Long id);

    @Query(value = "select b from BasketsEntity b" +
			" left join fetch b.ordersEntity o " +
			" left join fetch o.metaOrder m " +
			" left join fetch m.user usr " +
			" where b.id in :ids and (m.organization.id = :orgId or usr.organizationId = :orgId)")
    List<BasketsEntity> findByIdIn(@Param("ids") List<Long> ids, @Param("orgId")Long orgId);

	@Query(value = "select b from BasketsEntity b" +
			" left join fetch b.ordersEntity o " +
			" left join fetch o.metaOrder m " +
			" left join fetch m.user usr " +
			" where b.id in :ids")
	List<BasketsEntity> findByIdIn(@Param("ids") List<Long> ids);

}
