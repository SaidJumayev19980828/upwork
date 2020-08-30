package com.nasnav.dao;

import java.util.List;
import java.util.Set;

import com.nasnav.persistence.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.dto.query.result.StockBasicData;

public interface BasketRepository extends JpaRepository<BasketsEntity, Long> {

    @Query("select basket from BasketsEntity basket where basket.ordersEntity.id = :orderId")
    List<BasketsEntity> findByOrdersEntity_Id(@Param("orderId") Long orderId);
    
    @Query("select count(p) from BasketsEntity e "
    		+ " join e.stocksEntity s "
    		+ " join s.productVariantsEntity v "
    		+ " join v.productEntity p "
    		+ " where p.id= :productId ")
    Long countByProductId(@Param("productId") Long productId);


    @Query("select count(e) from BasketsEntity e "
    		+ " join e.ordersEntity o"
    		+ " join e.stocksEntity s "
    		+ " join s.productVariantsEntity v "
    		+ " join v.productEntity p "
    		+ " where p.id= :productId "
    		+ " and o.status = :status ")
	Long countByProductIdAndOrderEntity_status(Long productId, Integer status);

    @Transactional
    @Modifying
    @Query("delete from BasketsEntity basket where basket.ordersEntity.id in :orderIdList")
	void deleteByOrderIdIn( @Param("orderIdList") List<Long> orderIdList);

	@Transactional
	@Modifying
	@Query("delete from BasketsEntity basket where basket.ordersEntity.id in :orderIdList " +
			"and basket.ordersEntity in (select o from OrdersEntity o where o.status = :status and o.organizationEntity.id = :orgId)")
	void deleteByOrderIdInAndOrganizationIdAndStatus( @Param("orderIdList") List<Long> orderIdList,
											 @Param("orgId") Long orgId,
										     @Param("status") Integer status);
	
	@Transactional
	@Modifying
	@Query("delete from BasketsEntity basket " +
			" where basket.ordersEntity in (select o from OrdersEntity o where o.status = :status and o.organizationEntity.id = :orgId)")
	void deleteByOrganizationIdAndStatus(  @Param("status") Integer status, @Param("orgId") Long orgId);

    List<BasketsEntity> findByOrdersEntity_IdIn(Set<Long> ordersIds);
    
    
    @Transactional
	@Modifying
	@Query("delete from BasketsEntity basket "
			+ " where basket.stocksEntity in "
			+ "		(select s from StocksEntity s where s.productVariantsEntity.productEntity.id in :idList and s.organizationEntity.id = :orgId) " +
			" and basket.ordersEntity in (select o from OrdersEntity o where o.status = :status and o.organizationEntity.id = :orgId)")
	void deleteByProductIdInAndOrganizationIdAndStatus( @Param("idList") List<Long> productIdList,
											 @Param("orgId") Long orgId,
										     @Param("status") Integer status);
    
    @Query("SELECT NEW com.nasnav.persistence.dto.query.result.StockBasicData(variant.id , shop.id , stock.id) "
    		+ " from BasketsEntity basket "
    		+ " left join basket.stocksEntity stock "
    		+ " left join stock.shopsEntity shop "
    		+ " left join stock.productVariantsEntity variant"
    		+ " WHERE basket.id = :id")
    StockBasicData getItemStockBasicDataById( @Param("id") Long id);

    @Query(value = "select b from BasketsEntity b" +
			" left join fetch b.ordersEntity o" +
			" left join fetch o.metaOrder m" +
			" where b.id in :ids and m.organization.id = :orgId")
    List<BasketsEntity> findByIdIn(@Param("ids") List<Long> ids, @Param("orgId")Long orgId);
}
