package com.nasnav.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.nasnav.persistence.StocksEntity;

public interface StockRepository extends CrudRepository<StocksEntity, Long> {

	List<StocksEntity> findByShopsEntity_IdAndProductVariantsEntity_Id( Long shopId, Long variantId);
	
	

	@Query("select stock from StocksEntity stock "
			+ " join stock.productVariantsEntity var "
			+ " join var.productEntity prod "
			+ " where prod.id= :productId  "
			+ " and stock.shopsEntity.id = :shopsId"
			)
	List<StocksEntity> findByProductIdAndShopsId(Long productId, Long shopsId);
	
	

	List<StocksEntity> findByShopsEntity_Id(Long id);
	
	

	@Query("select stock from StocksEntity stock "
			+ " left join stock.productVariantsEntity var "
			+ " left join var.productEntity prod "
			+ " where prod.id in :productIds  " )
	List<StocksEntity> findByProductIdIn(List<Long> productIds);


	StocksEntity getOne(Long stockId);
}
